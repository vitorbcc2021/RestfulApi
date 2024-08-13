package com.vitor.springboot.controllers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.vitor.springboot.dtos.ProductRecordDto;
import com.vitor.springboot.models.ProductModel;
import com.vitor.springboot.repository.ProductRepository;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import jakarta.validation.Valid;

@RestController
public class ProductController {
    
    @Autowired
    ProductRepository productRepository;

    @PostMapping("/products/")
    public ResponseEntity<ProductModel> saveProduct(@RequestBody @Valid ProductRecordDto productRecordDto){
        var productModel = new ProductModel();
        BeanUtils.copyProperties(productRecordDto, productModel);

        return ResponseEntity.status(HttpStatus.CREATED).body(productRepository.save(productModel));
    }

    @GetMapping("/products/")
    public ResponseEntity<List<ProductModel>> getAllProducts(){
        List<ProductModel> products = productRepository.findAll();

        if(!products.isEmpty()){
            for(ProductModel product : products){
                UUID id = product.getIdProduct();
                product.add(linkTo(methodOn(ProductController.class).getOneProduct(id)).withSelfRel());
            }

        }
        return ResponseEntity.status(HttpStatus.OK).body(products);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Object> getOneProduct(@PathVariable("id") UUID id){
        Optional<ProductModel> productO = productRepository.findById(id);

        if(productO.isEmpty()) 
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
               
        productO.get().add(linkTo(methodOn(ProductController.class).getAllProducts()).withRel("Product List"));    

        return ResponseEntity.status(HttpStatus.OK).body(productO.get());
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<Object> updateProduct(@PathVariable("id") UUID id, @RequestBody @Valid ProductRecordDto productRecordDto){
        Optional<ProductModel> productO = productRepository.findById(id);

        if(productO.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found!");

        ProductModel product = productO.get();
        BeanUtils.copyProperties(productRecordDto, product);

        return ResponseEntity.status(HttpStatus.OK).body(productRepository.save(product));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Object> deleteProduct(@PathVariable("id") UUID id){
        Optional<ProductModel> productO = productRepository.findById(id);

        if(productO.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found!");
        }

        ProductModel product = productO.get();
        productRepository.delete(product);

        return ResponseEntity.status(HttpStatus.OK).body("Product Deleted Successfully!");
    }

    
}
