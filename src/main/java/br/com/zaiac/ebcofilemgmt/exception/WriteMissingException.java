package br.com.zaiac.ebcofilemgmt.exception;

public class WriteMissingException extends Exception {
    public WriteMissingException() {
    }

    public WriteMissingException(String msg) {
        super(msg);
    }

}
