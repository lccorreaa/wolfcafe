package edu.ncsu.csc326.wolfcafe.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Entity class for Tax Rate. The tax rate is a singleton; there is only one
 * row in the database that holds the current tax rate as a percent.
 *
 * @author Sandra Jose (sajose)
 */
@Entity
public class TaxRate {

    /** default tax rate percent used until an admin sets one */
    public static final double DEFAULT_RATE = 6.25;

    /** id for tax rate */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** value of the tax rate as a percent (e.g. 6.25 for 6.25%) */
    private Double rate;

    /**
     * default non parameterized constructor for tax rate; sets the rate to the
     * default and leaves the id unset so the database generates it
     */
    public TaxRate(){
        this.rate = DEFAULT_RATE;
    }

    /**
     * parameterized constructor for tax rate
     * @param id of tax rate
     * @param rate value of tax rate
     */
    public TaxRate(Long id, Double rate){
        this.id = id;
        this.rate = rate;
    }

    /**
     * getter method for tax rate id
     * @return entity id
     */
    public Long getId(){
        return id;
    }

    /**
     * setter method for tax rate id
     * @param id set field id to this id
     */
    public void setId(Long id){
        this.id = id;
    }

    /**
     * getter method for tax rate
     * @return entity rate
     */
    public Double getRate(){
        return rate;
    }

    /**
     * setter method for tax rate
     * @param rate set field rate to this rate
     */
    public void setRate(Double rate){
        this.rate = rate;
    }
}
