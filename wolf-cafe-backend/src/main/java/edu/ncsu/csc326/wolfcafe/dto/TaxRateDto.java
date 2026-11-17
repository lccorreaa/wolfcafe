package edu.ncsu.csc326.wolfcafe.dto;

/**
 * Dto class for Tax Rate
 *
 * @author Sandra Jose (sajose)
 */
public class TaxRateDto {

    /** id for tax rate */
    private Long id;

    /** value of the tax rate */
    private Double rate;

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
