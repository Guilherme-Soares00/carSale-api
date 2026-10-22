package br.com.fiap.carsaleapi.domain.car;

import br.com.fiap.carsaleapi.exception.CarNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CarService {
    private final CarRepository cars;

    public CarService(CarRepository cars) {
        this.cars = cars;
    }

    public List<CarResponse> getAllCars() {
        return cars.findAll().stream().map(CarResponse::fromModel).toList();
    }

    public CarResponse getCarById(Long id) {
        return CarResponse.fromModel(cars.findById(id).orElseThrow(() -> new CarNotFoundException(id)));
    }
}
