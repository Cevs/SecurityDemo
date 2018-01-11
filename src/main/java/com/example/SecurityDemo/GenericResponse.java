package com.example.SecurityDemo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.List;

public class GenericResponse {
    private String message;
    private String error;

    public GenericResponse(String message){
        super();
        this.message = message;
    }

    public GenericResponse(String message, String error){
        super();
        this.message = message;
        this.error = error;
    }

    public GenericResponse(List<FieldError> fieldErrors, List<ObjectError> globalErrors) {
        super();
        ObjectMapper mapper = new ObjectMapper();
        try{
            this.message = mapper.writeValueAsString(fieldErrors);
            this.error = mapper.writeValueAsString(globalErrors);
        }catch (JsonProcessingException e){
            this.message = "";
            this.error = "";
        }
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
