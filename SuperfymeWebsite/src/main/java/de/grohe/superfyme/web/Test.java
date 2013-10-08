/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.grohe.superfyme.web;


import javax.inject.Named;

/**
 *
 * @author Arthur.Grohe
 */
@Named
public class Test {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Creates a new instance of Test
     */
    public Test() {
    }
}
