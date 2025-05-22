package org.example.stock.services;


import org.springframework.data.repository.ListCrudRepository;

public interface PortfolioService extends ListCrudRepository<PortfolioItem, Long> {


}
