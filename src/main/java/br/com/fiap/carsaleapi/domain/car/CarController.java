package br.com.fiap.carsaleapi.domain.car;

import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cars")
public class CarController {
    private final CarService cars;

    public CarController(CarService cars) {
        this.cars = cars;
    }

    @GetMapping
    public List<CarResponse> getAllCars() {
        return cars.getAllCars();
    }

    @GetMapping("/{id}")
    public CarResponse getCarById(@PathVariable @Positive Long id) {
        return cars.getCarById(id);
    }
}
