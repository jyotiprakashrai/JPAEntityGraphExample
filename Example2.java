/*
In a case of Named Query, we have to define @NamedQuery with JPQL 
query at the entity, and after that, we need to build JOIN FETCH for associations:
*/

@Entity
@NamedQueries({
        @NamedQuery(name = "companyWithDepartmentsNamedQuery",
                query = "SELECT DISTINCT c " +
                        "FROM Company c " +
                        "LEFT JOIN FETCH c.departments " +
                        "WHERE c.id = :id")
})

/*
Then, we need to invoke this query:
*/
@Override
public Company getCompanyWithDepartments(Long companyId) {
    Query query = entityManager.createNamedQuery("companyWithDepartmentsNamedQuery")
            .setParameter("id", companyId);

    return (Company) DataAccessUtils.singleResult(query.getResultList());
}

/*
Named Entity Graph
Now we are going to implement it with a new mechanism of JPA 2.1. 
The definition of a named entity graph is done by the @NamedEntityGraph annotation at the entity.
It defines a unique name and a list of attributes (the attributeNodes) which need to be loaded.
*/
@NamedEntityGraphs({
        @NamedEntityGraph(name = "companyWithDepartmentsGraph",
                attributeNodes = {@NamedAttributeNode("departments")})
})

/*
As you can see, we just listed attributes (relationships) that need to be queried.
In above annotation we have defined entity graph, so now we can use it in a query.
We have to create a Map with query hints and set it as an additional parameter on a find or query method call. 
Below code shows how we can use an entity graph as a fetch graph in a find method.
*/

@Override
public Company getCompanyWithDepartments(Long companyId) {
    EntityGraph graph = entityManager.getEntityGraph("companyWithDepartmentsGraph");
    Map<String, Object> hints = new HashMap<>();
    hints.put("javax.persistence.fetchgraph", graph);

    return entityManager.find(Company.class, companyId, hints);
}


/*
Query for sub-graph
In next example let’s query for particular Company with all related departments and all relevant employees, 
so we are going to implement a getCompanyWithDepartmentsAndEmployees method.

With Criteria API we have to create CriteriaQuery, fetch departments attribute and then fetch employees attribute from it:
*/
@Override
public Company getCompanyWithDepartmentsAndEmployees(Long companyId) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Company> query = builder.createQuery(Company.class);

    Root<Company> root = query.from(Company.class);
    Fetch<Company, Department> departmentFetch = root.fetch(Company_.departments, JoinType.LEFT);
    departmentFetch.fetch(Department_.employees, JoinType.LEFT);

    query.select(root).distinct(true);
    Predicate idPredicate = builder.equal(root.get(Company_.id), companyId);
    query.where(builder.and(idPredicate));

    return DataAccessUtils.singleResult(entityManager.createQuery(query).getResultList());
}


/*
With Named Query, we need to define JPQL query, fetch departments, employees:
*/

@Entity
@NamedQueries({
        @NamedQuery(name = "companyWithDepartmentsAndEmployeesNamedQuery",
                query = "SELECT DISTINCT c " +
                        "FROM Company c " +
                        "LEFT JOIN FETCH c.departments as d " +
                        "LEFT JOIN FETCH d.employees " +
                        "WHERE c.id = :id")
})


/*
Then, we need to invoke our query:
*/

@Override
public Company getCompanyWithDepartmentsAndEmployees(Long companyId) {
    Query query = entityManager.createNamedQuery("companyWithDepartmentsAndEmployeesNamedQuery")
            .setParameter("id", companyId);

    return (Company) DataAccessUtils.singleResult(query.getResultList());
}

/*
Named subgraph
For querying company with all departments, we had to define @NamedEntityGraph, 
but if we want to query relationships from Department, we have to define Company entity subgraph –@NamedSubgraph. 
The definition of a named subgraph is similar to the definition of a named entity graph and can be referenced as an attributeNode.

The following code shows usage of subgraph:
*/

@NamedEntityGraphs({
        @NamedEntityGraph(name = "companyWithDepartmentsAndEmployeesGraph",
                attributeNodes = {@NamedAttributeNode(value = "departments", subgraph = "departmentsWithEmployees")},
                subgraphs = @NamedSubgraph(
                        name = "departmentsWithEmployees",
                        attributeNodes = @NamedAttributeNode("employees")))
})

/*
Invocation of above query looks like:
*/
@Override
public Company getCompanyWithDepartmentsAndEmployees(Long companyId) {
    EntityGraph graph = entityManager.getEntityGraph("companyWithDepartmentsAndEmployeesGraph");
    Map<String, Object> hints = new HashMap<>();
    hints.put("javax.persistence.fetchgraph", graph);

    return entityManager.find(Company.class, companyId, hints);
}

/*
Dynamic Entity Graph:
##########################
Besides static graph queries, it is possible to implement dynamic entity graph.
We will define simple entity graph that tells the entity manager to fetch Company with all associated Cars.

We have to use the createEntityGraph(Class rootType) method of the entity manager to create an entity 
graph for the Company entity and in next step define a list of attributes to be fetched. Our dynamic query looks like:
*/

@Override
public Company getCompanyWithCars(Long companyId) {
    EntityGraph<Company> graph = entityManager.createEntityGraph(Company.class);
    graph.addAttributeNodes("cars");

    Map<String, Object> hints = new HashMap<>();
    hints.put("javax.persistence.loadgraph", graph);

    return entityManager.find(Company.class, companyId, hints);
}

/*

*/

