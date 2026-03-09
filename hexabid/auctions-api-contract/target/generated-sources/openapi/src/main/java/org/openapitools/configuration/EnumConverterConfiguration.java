package org.openapitools.configuration;

import com.acme.auctions.contract.model.AuctionSort;
import com.acme.auctions.contract.model.AuctionStatus;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

@Configuration(value = "org.openapitools.configuration.enumConverterConfiguration")
public class EnumConverterConfiguration {

    @Bean(name = "org.openapitools.configuration.EnumConverterConfiguration.auctionSortConverter")
    Converter<String, AuctionSort> auctionSortConverter() {
        return new Converter<String, AuctionSort>() {
            @Override
            public AuctionSort convert(String source) {
                return AuctionSort.fromValue(source);
            }
        };
    }
    @Bean(name = "org.openapitools.configuration.EnumConverterConfiguration.auctionStatusConverter")
    Converter<String, AuctionStatus> auctionStatusConverter() {
        return new Converter<String, AuctionStatus>() {
            @Override
            public AuctionStatus convert(String source) {
                return AuctionStatus.fromValue(source);
            }
        };
    }

}
