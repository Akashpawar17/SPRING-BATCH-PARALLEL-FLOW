/* package com.batch.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class DBConfiguration {
	@Autowired
	DataSource dataSource;
	
	 @Bean
	    public DataSourceInitializer mysqlDatabasePopulator() {
	        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
	        log.info("inserting into h2...............");
	        populator.addScript(
	                new ClassPathResource("Schema.sql"));
	        populator.setContinueOnError(true);
	        populator.setIgnoreFailedDrops(true);

	        DataSourceInitializer initializer = new DataSourceInitializer();
	        initializer.setDatabasePopulator(populator);
	        initializer.setDataSource(dataSource);

	        return initializer;
	    }
}
*/
