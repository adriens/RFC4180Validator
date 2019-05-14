/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.adriens.rfc4180validator;

/**
 *
 * @author 3004SAL
 */
public class NotUTF8EncodedException  extends Exception{
    public NotUTF8EncodedException(String errorMessage){
        super(errorMessage);
    }
}
