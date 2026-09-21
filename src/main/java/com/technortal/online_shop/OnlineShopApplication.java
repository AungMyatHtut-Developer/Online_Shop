package com.technortal.online_shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OnlineShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineShopApplication.class, args);
    }

}


// Online Shop Project
// Product(id, product name, price, quantity,product_image(blob), created_date, updated_date)
// insert, update, delete, retrieve
// MVC -> Model -> View -> Controller

// JavaEE(JakatarEE) , Spring Framework (Bean) (IOC Container) (DI) -> Spring Boot
// Codex (USD 100)

// Model(DB) -> Controller -> View

/*
I would like to create an "Online Shop Web App".
You have to follow my structured packages design and the project will be MVC.
The main entity is Product. (Product must contain those columns:
id, product name, price, quantity, product_image(blob), created_date, updated_date.
Create login UI with fixed username and password (admin:1234). UI theme must use blue and white.
Create two controller : LoginController and Product Controller.
Create only those things because I am teaching my students project creation step by step.

 */
