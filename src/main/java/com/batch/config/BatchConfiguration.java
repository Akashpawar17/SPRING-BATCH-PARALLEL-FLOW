package com.batch.config;

import java.util.concurrent.Executor;

import javax.sql.DataSource;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import com.batch.exception.MissingUsernameException;
import com.batch.exception.NegativeAmountException;
import com.batch.pojo.Employee;

@Configuration
public class BatchConfiguration {
	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Value("classPath:/csv/inputD.csv")
	private Resource inputResource;

	@Autowired
	DataSource dataSource;

	@Bean
	public Job readCSVFileJob() {

		return jobBuilderFactory.get("readCSVFileJob")
				.incrementer(new RunIdIncrementer())
				.start(splitFlow())
				.build().//builds FlowJobBuilder instance
				build();//builds Job instance
		/*
		 * Flow flow2=new FlowBuilder<Flow>("flow2").start(step2()).build();
		 * 
		 * Flow flow1=new FlowBuilder<Flow>("flow1").start(step()).split(new
		 * SimpleAsyncTaskExecutor()).add(flow2).build(); return jobBuilderFactory
		 * .get("readCSVFileJob") .incrementer(new RunIdIncrementer()) .start(flow1)
		 * .end() .build();
		 */

	}
	//Parallel step
	
	@Bean
	public Flow splitFlow() {
	    return new FlowBuilder<SimpleFlow>("splitFlow")
	        .split(simpleTaskExecutor())
	        .add(flow1(), flow2())
	        .build();
	}
	@Bean
	public Flow flow1() {
		  return new FlowBuilder<SimpleFlow>("flow1")
				  .start(step())
				  .next(step2())
				  .end();
	}
	

	@Bean
	public Flow flow2() {
		return new FlowBuilder<SimpleFlow>("flow2")
				  .start(step3())
				 
				  .end();
	}
	
	// Step1
	// First of all, to enable skip functionality, we need to include a call to
	// faultTolerant() during the step-building process.
//Within skip() and skipLimit(), we define the exceptions we want to skip and the maximum number of skipped items.
	@Bean
	public Step step() {
		return stepBuilderFactory.get("step").<Employee, Employee>chunk(1).reader(reader(null)).processor(processor())
				.writer(writer())
				// taskExecutor(simpleTaskExecutor())
				// .faultTolerant().retryLimit(3).retry(DeadlockLoserDataAccessException.class)
				// skipPolicy(skipPolicy())
				// skipLimit(3).skip(Exception.class)
				.build();
	}

//The core pool size says a thread pool executor will start with N number of threads. A throttle-limit T says that, regardless of the number of threads available in the thread pool, only use T of those threads for a tasklet.
	public JobSkipPolicy skipPolicy() {
		return new JobSkipPolicy();
	}

	// Step2

	@Bean
	public Step step2() {
		return stepBuilderFactory.get("step2").<Employee, Employee>chunk(10).reader(reader(null)).processor(processor())
				.writer(writer()).build();
	}
	@Bean
	public Step step3() {
		return stepBuilderFactory.get("step3").<Employee, Employee>chunk(10).reader(reader(null)).processor(processor())
				.writer(writer()).build();
	}
	@Bean
	public TaskExecutor simpleTaskExecutor() {
		/*
		 * ThreadPoolTaskExecutor executor=new ThreadPoolTaskExecutor();
		 * executor.setCorePoolSize(5); executor.setMaxPoolSize(5);
		 * executor.setThreadNamePrefix("default_task_executor_thread");
		 */
		SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
		taskExecutor.setConcurrencyLimit(5);

		return taskExecutor;
	}

	@Bean
	public ItemProcessor<Employee, Employee> processor() {
		return new DBLogProcessor();
	}

//Job scope, introduced in Spring Batch 3.0, is similar to Step scope in configuration but is a Scope for the Job context, 
	// so that there is only one instance of such a bean per running job.
	// Using a scope of Step is required in order to use late binding, because the
	// bean cannot actually be instantiated until the Step starts, to allow the
	// attributes to be found. Because it is not part of the Spring container by
	// default, the scope must be added explicitly, by using the batch namespace or
	// by including a bean definition explicitly for the StepScope
	@Bean
	@StepScope
	public FlatFileItemReader<Employee> reader(@Value("#{jobParameters['inputResource']}") String inputResource) {// @Value("#{jobParameters['inputResource']}")
																													// String
																													// inputResource
		FlatFileItemReader<Employee> itemReader = new FlatFileItemReader<Employee>();
		itemReader.setLineMapper(lineMapper());
		itemReader.setLinesToSkip(1);
		itemReader.setResource(new FileSystemResource(inputResource));
		itemReader.setStrict(false);
		return itemReader;
	}

	@Bean
	public LineMapper<Employee> lineMapper() {
		DefaultLineMapper<Employee> lineMapper = new DefaultLineMapper<Employee>();
		DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
		lineTokenizer.setNames(new String[] { "id", "firstName", "lastName" });
		lineTokenizer.setIncludedFields(new int[] { 0, 1, 2 });
		BeanWrapperFieldSetMapper<Employee> fieldSetMapper = new BeanWrapperFieldSetMapper<Employee>();
		fieldSetMapper.setTargetType(Employee.class);
		lineMapper.setLineTokenizer(lineTokenizer);
		lineMapper.setFieldSetMapper(fieldSetMapper);
		return lineMapper;
	}

	@Bean
	public JdbcBatchItemWriter<Employee> writer() {
		JdbcBatchItemWriter<Employee> itemWriter = new JdbcBatchItemWriter<Employee>();
		itemWriter.setDataSource(dataSource);
		itemWriter.setSql("INSERT INTO EMPLOYEE (ID, FIRSTNAME, LASTNAME) VALUES (:id, :firstName, :lastName)");
		itemWriter.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Employee>());
		return itemWriter;
	}

}
