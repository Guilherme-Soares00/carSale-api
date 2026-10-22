package br.com.fiap.carsaleapi.exception;

public class CarNotFoundException extends RuntimeException {
    public CarNotFoundException(Long id) {
        super("Car not found with id: " + id);
    }
}
