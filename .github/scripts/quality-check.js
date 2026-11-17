// Reads one report produced by the build and turns each problem in it into a
// GitHub annotation, so it shows on the run page and beside the offending line
// in a pull request's "Files changed" tab.  Writes a table of every problem to
// the run summary, and exits non-zero if there are any, which turns the
// Quality check red.
//
// Run by the Quality job in .github/workflows/build.yml:
//
//   node quality-check.js checkstyle <dir>
//   node quality-check.js eslint <dir>
//   node quality-check.js coverage <dir> <line-minimum-%> <branch-minimum-%>
//
// <dir> is where the job downloaded the report artifact.  The report is found
// by file name anywhere beneath it, so a change to the artifact's folder layout
// does not break the check.
//
// Uses only Node's standard library; nothing to install.

const fs = require('fs');
const path = require('path');

// GitHub shows at most this many annotations of each kind per step.  The rest
// are still listed in the run summary table.
const ANNOTATION_LIMIT = 10;

// Keeps a huge first run from producing an unreadable summary page.
const SUMMARY_ROW_LIMIT = 100;

function findFile(dir, name) {
  if (!fs.existsSync(dir)) {
    return null;
  }
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      const found = findFile(full, name);
      if (found) {
        return found;
      }
    } else if (entry.name === name) {
      return full;
    }
  }
  return null;
}

function summary(text) {
  console.log(text);
  if (process.env.GITHUB_STEP_SUMMARY) {
    fs.appendFileSync(process.env.GITHUB_STEP_SUMMARY, text + '\n');
  }
}

// Workflow command escaping, per GitHub's documentation.
function escapeData(s) {
  return String(s).replace(/%/g, '%25').replace(/\r/g, '%0D').replace(/\n/g, '%0A');
}

function escapeProperty(s) {
  return escapeData(s).replace(/:/g, '%3A').replace(/,/g, '%2C');
}

function annotate(level, { file, line, col, title }, message) {
  const props = [];
  if (file) props.push(`file=${escapeProperty(file)}`);
  if (line) props.push(`line=${escapeProperty(line)}`);
  if (col) props.push(`col=${escapeProperty(col)}`);
  if (title) props.push(`title=${escapeProperty(title)}`);
  console.log(`::${level} ${props.join(',')}::${escapeData(message)}`);
}

// The tools write absolute paths from the build machine.  Annotations need a
// path relative to the repository root, which starts at the module folder.
function repoPath(absolute, moduleDir) {
  const normalized = absolute.replace(/\\/g, '/');
  const index = normalized.lastIndexOf(`/${moduleDir}/`);
  return index >= 0 ? normalized.substring(index + 1) : normalized;
}

function unescapeXml(s) {
  return s
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&quot;/g, '"')
    .replace(/&apos;/g, "'")
    .replace(/&#(\d+);/g, (m, code) => String.fromCharCode(Number(code)))
    .replace(/&amp;/g, '&');
}

function xmlAttributes(tag) {
  const attributes = {};
  for (const match of tag.matchAll(/(\w+)="([^"]*)"/g)) {
    attributes[match[1]] = unescapeXml(match[2]);
  }
  return attributes;
}

function cell(s) {
  return String(s).replace(/\|/g, '\\|').replace(/\r?\n/g, ' ');
}

function missingReport(tool, name, job) {
  annotate('error', { title: `${tool} not checked` },
    `No ${name} was found, so ${tool} could not be checked.  The "${job}" job ` +
    'failed before producing it - fix that job first.');
  summary(`### ${tool}\n\n:x: No ${name} was found.  See the **${job}** job.\n`);
  process.exit(1);
}

// Lists every problem in the summary, annotates the first few, and fails the
// step if there are any.
function report(tool, problems, howToRun) {
  if (problems.length === 0) {
    summary(`### ${tool}\n\n:white_check_mark: No problems found.\n`);
    return;
  }

  const counts = {};
  problems.forEach((p) => {
    counts[p.level] = (counts[p.level] || 0) + 1;
    if (counts[p.level] <= ANNOTATION_LIMIT) {
      annotate(p.level, p, p.message);
    }
  });

  const lines = [
    `### ${tool}`,
    '',
    `:x: **${problems.length}** problem${problems.length === 1 ? '' : 's'} found.  ` +
      `Run \`${howToRun}\` to see them locally.`,
    '',
    '| Location | Severity | Rule | Problem |',
    '| --- | --- | --- | --- |',
  ];
  for (const p of problems.slice(0, SUMMARY_ROW_LIMIT)) {
    lines.push(`| \`${cell(p.file)}:${p.line || ''}\` | ${p.level} | ${cell(p.title)} | ${cell(p.message)} |`);
  }
  if (problems.length > SUMMARY_ROW_LIMIT) {
    lines.push('', `...and ${problems.length - SUMMARY_ROW_LIMIT} more.`);
  }
  summary(lines.join('\n') + '\n');
  process.exit(1);
}

function checkstyle(dir) {
  const file = findFile(dir, 'checkstyle-result.xml');
  if (!file) {
    missingReport('Checkstyle', 'Checkstyle report', 'Build backend');
  }
  const xml = fs.readFileSync(file, 'utf8');
  const problems = [];
  for (const block of xml.matchAll(/<file\s+name="([^"]*)"\s*>([\s\S]*?)<\/file>/g)) {
    const source = repoPath(unescapeXml(block[1]), 'wolf-cafe-backend');
    for (const tag of block[2].matchAll(/<error\s[^>]*\/>/g)) {
      const a = xmlAttributes(tag[0]);
      problems.push({
        level: 'warning',
        file: source,
        line: a.line,
        col: a.column,
        // com.puppycrawl...checks.javadoc.MissingJavadocMethodCheck -> MissingJavadocMethod
        title: `Checkstyle: ${(a.source || '').split('.').pop().replace(/Check$/, '')}`,
        message: a.message,
      });
    }
  }
  report('Checkstyle', problems, './mvnw checkstyle:check');
}

function eslint(dir) {
  const file = findFile(dir, 'eslint-report.json');
  if (!file) {
    missingReport('ESLint', 'ESLint report', 'Lint frontend');
  }
  const results = JSON.parse(fs.readFileSync(file, 'utf8'));
  const problems = [];
  for (const result of results) {
    const source = repoPath(result.filePath, 'wolf-cafe-frontend');
    for (const m of result.messages) {
      problems.push({
        level: m.severity === 2 ? 'error' : 'warning',
        file: source,
        line: m.line,
        col: m.column,
        // A file ESLint could not parse has no rule.
        title: `ESLint: ${m.ruleId || 'parse error'}`,
        message: m.message,
      });
    }
  }
  report('ESLint', problems, 'npm run lint');
}

function coverage(dir, lineMinimum, branchMinimum) {
  const file = findFile(dir, 'jacoco.xml');
  if (!file) {
    missingReport('Coverage', 'coverage report', 'Build backend');
  }
  const xml = fs.readFileSync(file, 'utf8');

  // Counters appear for every method, class, and package; the project totals
  // are the last one of each type in the file.
  function percent(type) {
    const all = [...xml.matchAll(new RegExp(`<counter type="${type}" missed="(\\d+)" covered="(\\d+)"\\s*/>`, 'g'))];
    if (all.length === 0) {
      return 0;
    }
    const [, missed, covered] = all[all.length - 1].map(Number);
    const total = missed + covered;
    // A project with no branches at all has nothing to miss.
    return total === 0 ? 100 : (covered * 100) / total;
  }

  const rows = [
    { name: 'Line', actual: percent('LINE'), minimum: lineMinimum },
    { name: 'Branch', actual: percent('BRANCH'), minimum: branchMinimum },
  ];
  const short = rows.filter((r) => r.actual < r.minimum);

  for (const r of short) {
    annotate('error', { title: `${r.name} coverage below ${r.minimum}%` },
      `${r.name} coverage is ${r.actual.toFixed(1)}%, below the ${r.minimum}% minimum.  ` +
      'Open wolf-cafe-backend/target/site/jacoco-ut/index.html after ./mvnw test to see what the tests miss.');
  }

  const lines = [
    '### Coverage',
    '',
    short.length === 0 ? ':white_check_mark: Coverage meets the minimums.' : ':x: Coverage is below the minimums.',
    '',
    '| Measure | Coverage | Minimum | |',
    '| --- | ---: | ---: | --- |',
    ...rows.map((r) => `| ${r.name} | ${r.actual.toFixed(1)}% | ${r.minimum}% | ${r.actual < r.minimum ? ':x:' : ':white_check_mark:'} |`),
  ];
  summary(lines.join('\n') + '\n');
  if (short.length > 0) {
    process.exit(1);
  }
}

const [mode, dir, ...rest] = process.argv.slice(2);
switch (mode) {
  case 'checkstyle':
    checkstyle(dir);
    break;
  case 'eslint':
    eslint(dir);
    break;
  case 'coverage':
    coverage(dir, Number(rest[0]), Number(rest[1]));
    break;
  default:
    console.error('Usage: node quality-check.js checkstyle|eslint|coverage <report-dir> [line-min branch-min]');
    process.exit(2);
}
