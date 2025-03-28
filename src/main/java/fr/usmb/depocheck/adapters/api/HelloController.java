package fr.usmb.depocheck.adapters.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Marks this class as a REST controller
@RestController
@RequestMapping("/api")
public class HelloController {

    // Maps HTTP GET requests to /greeting to this method
    @GetMapping("/greeting")
    public ResponseEntity<String> greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        // Returns a greeting message, using the provided name or "World" by default
        return new ResponseEntity<>(String.format("Hello, %s!", name), HttpStatus.OK);
    }
}