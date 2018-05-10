@Override
public Company getCompanyWithDepartments(Long companyId) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Company> query = builder.createQuery(Company.class);

    Root<Company> root = query.from(Company.class);
    root.fetch(Company_.departments, JoinType.LEFT);

    query.select(root).distinct(true);
    Predicate idPredicate = builder.equal(root.get(Company_.id), companyId);
    query.where(builder.and(idPredicate));

    return DataAccessUtils.singleResult(entityManager.createQuery(query).getResultList());
}
