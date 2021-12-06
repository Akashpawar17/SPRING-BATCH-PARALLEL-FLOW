package com.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableScheduling
@EnableBatchProcessing
@SpringBootApplication
public class IpseBatchAppApplication {

	
	Logger logger=LoggerFactory.getLogger(IpseBatchAppApplication.class);
	public static void main(String[] args) {
		String[] newArgs=new String[] {"inputResource=C:/Akash/demo.txt"};
		SpringApplication.run(IpseBatchAppApplication.class, newArgs);
	}
	 
	
	
	
	
	//@Autowired
	   // JobLauncher jobLauncher;
	      
	   // @Autowired
	    //Job job;
	  //@Scheduled(cron = "*/30 * * * * *")
	   // public BatchStatus perform() throws Exception
	   // {
	    //    JobParameters params = new JobParametersBuilder()
	       //         .addString("JobId", String.valueOf(System.currentTimeMillis()))
	        		//.addString("inputResource", "C://demo//input.txt")
	              //  .toJobParameters();
	    // JobExecution jobExecution= jobLauncher.run(job, params);
	    // System.out.println("Demoo..");
	     //while (jobExecution.isRunning()) {
	    //	 System.out.println("Running");
			
		//}
		//return jobExecution.getStatus();
	     
	   // }
}

