package com.fullstackproject.services.customer;

import com.fullstackproject.dto.BookDto;
import com.fullstackproject.dto.CarDto;
import com.fullstackproject.dto.CarListDto;
import com.fullstackproject.dto.SearchDto;
import com.fullstackproject.entities.Book;
import com.fullstackproject.entities.Car;
import com.fullstackproject.entities.User;
import com.fullstackproject.enums.BookingStatus;
import com.fullstackproject.repositories.BookRepository;
import com.fullstackproject.repositories.CarRepository;
import com.fullstackproject.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Override
    public List<CarDto> getAllCars() {
        return carRepository.findAll().stream().map(Car::getCarDto).collect(Collectors.toList());
    }

    @Override
    public boolean bookCar(BookDto bookDto) {
        Optional<Car> optionalCar = carRepository.findById(bookDto.getCarId());
        Optional<User> optionalUser = userRepository.findById(bookDto.getUserId());

        if (optionalCar.isPresent() && optionalUser.isPresent()) {
            Car existingCar = optionalCar.get();
            Book book = new Book();
            book.setUser(optionalUser.get());
            book.setCar(existingCar);
            book.setBookingStatus(BookingStatus.PENDING);
            long diffInMilliSeconds = bookDto.getToDate().getTime() - bookDto.getFromDate().getTime();
            long days = TimeUnit.MILLISECONDS.toDays(diffInMilliSeconds);
            book.setDays(days);
            book.setPrice(existingCar.getPrice() * days);
            book.setFromDate(bookDto.getFromDate());
            book.setToDate(bookDto.getToDate());
            bookRepository.save(book);
            return true;
        }
        return false;
    }

    @Override
    public CarDto getCarById(Long carId) {
        Optional<Car> optionalCar = carRepository.findById(carId);
        return optionalCar.map(Car::getCarDto).orElse(null);
    }

    @Override
    public List<BookDto> getBookingByUserId(Long userId) {
        return bookRepository.findAllByUserId(userId).stream().map(Book::getBookCarDto).collect(Collectors.toList());
    }

    @Override
    public CarListDto searchCar(SearchDto searchDto) {
        Car car = new Car();
        car.setBrand(searchDto.getBrand());
        car.setType(searchDto.getType());
        car.setTransmission(searchDto.getTransmission());
        car.setColor(searchDto.getColor());
        ExampleMatcher exampleMatcher = ExampleMatcher.matchingAll()
                .withMatcher("brand",ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("type",ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("transmission",ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("color",ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
        Example<Car> carExample = Example.of(car, exampleMatcher);
        List<Car> carList = carRepository.findAll(carExample);
        CarListDto carListDto = new CarListDto();
        carListDto.setCarDtoList(carList.stream().map(Car::getCarDto).collect(Collectors.toList()));
        return carListDto;
    }
}
