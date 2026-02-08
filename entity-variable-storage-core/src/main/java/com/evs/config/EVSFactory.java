package com.evs.config;

import com.evs.repository.EntityInstanceRepository;
import com.evs.repository.EntityRepository;
import com.evs.repository.VariableRepository;
import com.evs.service.EntityInstanceService;
import com.evs.service.EntityService;
import com.evs.service.PartitionManager;
import com.evs.service.VariableService;

import javax.sql.DataSource;

/**
 * Factory for creating EVS components. Use this for standalone configuration.
 */
public class EVSFactory {

    private final DataSource dataSource;
    private final EntityRepository entityRepository;
    private final EntityInstanceRepository entityInstanceRepository;
    private final VariableRepository variableRepository;
    private final PartitionManager partitionManager;

    public EVSFactory(EVSConfig config) {
        this.dataSource = config.createDataSource();
        config.runMigrations(dataSource);

        this.entityRepository = new EntityRepository(dataSource);
        this.entityInstanceRepository = new EntityInstanceRepository(dataSource);
        this.variableRepository = new VariableRepository(dataSource);
        this.partitionManager = new PartitionManager(dataSource);
    }

    /**
     * Create factory with existing DataSource (e.g. from Spring). Migrations are skipped.
     */
    public EVSFactory(DataSource dataSource) {
        this.dataSource = dataSource;
        this.entityRepository = new EntityRepository(dataSource);
        this.entityInstanceRepository = new EntityInstanceRepository(dataSource);
        this.variableRepository = new VariableRepository(dataSource);
        this.partitionManager = new PartitionManager(dataSource);
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public EntityService entityService() {
        return new EntityService(entityRepository, partitionManager);
    }

    public EntityInstanceService entityInstanceService() {
        return new EntityInstanceService(entityInstanceRepository);
    }

    public VariableService variableService() {
        return new VariableService(variableRepository);
    }

    public PartitionManager partitionManager() {
        return partitionManager;
    }
}
