# WolfCafe

## Resources

  * [Environment Installation Instructions](https://courses.csc.ncsu.edu/csc326/install/)
  * [CoffeeMaker Setup Instructions](https://courses.csc.ncsu.edu/csc326/onboarding/setup/) - WolfCafe is set up the same way

## WolfCafe Technical Documentation

  * [CoffeeMaker Investigation](technical-documents/coffeemaker-investigation.md)
  * [Requirements](technical-documents/requirements/index.md)
  * [Design](technical-documents/design/index.md)
     * [Security and Logging](technical-documents/design/security.md) - user roles, login, and the audit log
     * [REST API](technical-documents/design/rest-api.md)
  * [System Tests](technical-documents/system-tests.md)
  * [Developers' Guide](technical-documents/developers-guide.md)
  * [Users' Guide](technical-documents/users-guide.md)
  * [Privacy Policy](technical-documents/privacy-policy.md)
  * [Human Flourishing](technical-documents/human-flourishing.md)
  * [Extra Credit](technical-documents/extra-credit.md)

## Technical Process

  * [GitHub Process](technical-documents/github-process.md)
  * [Static Analysis](technical-documents/static-analysis.md)
  * [Maven Build](technical-documents/maven-build.md)

## Project Management

  * [Project Management Plan](project-management/index.md)

## GitHub Pages

You can view your project's documentation through a private GitHub Pages site.  To find the site URL (which is automatically generated), go to your repo in github.com and select the Actions tab.  Select the last build named "pages build and deployment" and click the link under the deploy action.  Add the link to your README.md file for easy access.

## `wolf-cafe-backend` Backend Quick Start

1. Open IntelliJ.  In IntelliJ, open your repository folder.

2. Right click on `wolf-cafe-backend/pom.xml` and select **Add as Maven Project**.

3. Maven Project Reload. Right click on `pom.xml` and select **Maven > Sync Project**.

4. Make sure MySQL is running.  There is nothing to configure: `application.properties` defaults to a local MySQL as `root` with no password, and the `wolfcafe` and `wolfcafe_test` schemas are created on first use.
     * If your MySQL uses a different host, port, user, or password, copy `wolf-cafe-backend/.env.template` to `wolf-cafe-backend/.env` and edit it.  `.env` is gitignored, so your settings stay on your machine.
     * Do not edit `application.properties`.  That file is committed, so editing it would push your local settings to the whole team.

5. Open `wolf-cafe-backend/src/main/java`.  Right click on `WolfCafeApplication` and select **Run WolfCafeApplication.main()**.  You may need to approve Lombok settings for the application.

On its first start, the backend creates the user roles and a default admin user with the username `admin` and the password `admin`.  To use a different admin password, set `ADMIN_PASSWORD` in `wolf-cafe-backend/.env` before the first start.  See [Security and Logging](technical-documents/design/security.md) for how users, roles, and logins work, and how to try the API in Postman.

## `wolf-cafe-frontend` Frontend Quick Start

1. Open a Terminal and change directory to `~/git/<repo_name>/wolf-cafe-frontend`

2. Update libraries. Run `npm install`

3. Run the application. Run `npm run dev`
     * The frontend expects the backend at `http://localhost:8080`.  To point it somewhere else, create `wolf-cafe-frontend/.env.local` containing `VITE_API_BASE_URL=http://localhost:9090`.  Vite reads `.env.local`, and `*.local` is already gitignored - that is why the frontend uses that name rather than the backend's `.env`.

4. Browse to http://localhost:3000/ and log in as `admin`, or register a new customer.
