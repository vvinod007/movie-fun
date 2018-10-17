package org.superbiz.moviefun;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import static org.springframework.orm.jpa.vendor.Database.MYSQL;


@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})

public class Application {

    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
    }

    @Bean
    public DatabaseServiceCredentials databaseServiceCredentials(@Value("${VCAP_SERVICES}") String vcapService){
        return new DatabaseServiceCredentials(vcapService);
    }

    @Bean
    public DataSource albumsDataSource(DatabaseServiceCredentials serviceCredentials) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(serviceCredentials.jdbcUrl("albums-mysql"));

        /*HikariConfig config = new HikariConfig();
        config.setDataSource(dataSource);
        return new HikariDataSource(config);*/

        HikariDataSource config = new HikariDataSource();
        config.setDataSource(dataSource);
        return config;
    }

    @Bean
    public DataSource moviesDataSource(DatabaseServiceCredentials serviceCredentials) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(serviceCredentials.jdbcUrl("movies-mysql"));
        //HikariConfig config = new HikariConfig();
        //config.setDataSource(dataSource);
        //return new HikariDataSource(config);

        HikariDataSource config = new HikariDataSource();
        config.setDataSource(dataSource);
        return config;
    }

    @Bean
    public HibernateJpaVendorAdapter hibernateJpaVendorAdapter(){
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter =  new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setDatabase(Database.MYSQL);
        hibernateJpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        hibernateJpaVendorAdapter.setGenerateDdl(true);
        return hibernateJpaVendorAdapter;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean_Album(
            DataSource albumsDataSource,
            HibernateJpaVendorAdapter hibernateJpaVendorAdapter) {
        LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean_Album = new LocalContainerEntityManagerFactoryBean();
        localContainerEntityManagerFactoryBean_Album.setDataSource(albumsDataSource);
        localContainerEntityManagerFactoryBean_Album.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        localContainerEntityManagerFactoryBean_Album.setPackagesToScan("org.superbiz.moviefun");
        localContainerEntityManagerFactoryBean_Album.setPersistenceUnitName("albums");
        return localContainerEntityManagerFactoryBean_Album;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean_Movies(
            DataSource moviesDataSource,
            HibernateJpaVendorAdapter hibernateJpaVendorAdapter){
        LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean_Movies = new LocalContainerEntityManagerFactoryBean();
        localContainerEntityManagerFactoryBean_Movies.setDataSource(moviesDataSource);
        localContainerEntityManagerFactoryBean_Movies.setJpaVendorAdapter(hibernateJpaVendorAdapter);
        localContainerEntityManagerFactoryBean_Movies.setPackagesToScan("org.superbiz.moviefun");
        localContainerEntityManagerFactoryBean_Movies.setPersistenceUnitName("movies");
        return localContainerEntityManagerFactoryBean_Movies;
    }

    @Bean
    public PlatformTransactionManager moviesPlatformTransactionManager(EntityManagerFactory localContainerEntityManagerFactoryBean_Movies) {
        return new JpaTransactionManager(localContainerEntityManagerFactoryBean_Movies);
    }

    @Bean
    public PlatformTransactionManager albumsPlatformTransactionManager(EntityManagerFactory localContainerEntityManagerFactoryBean_Album) {
        return new JpaTransactionManager(localContainerEntityManagerFactoryBean_Album);
    }

    @Bean
    public TransactionOperations transactionOperations_Albums(PlatformTransactionManager albumsPlatformTransactionManager){
        return new TransactionTemplate(albumsPlatformTransactionManager);
    }

    @Bean
    public TransactionOperations transactionOperations_Movies(PlatformTransactionManager moviesPlatformTransactionManager){
        return new TransactionTemplate(moviesPlatformTransactionManager);
    }
}
