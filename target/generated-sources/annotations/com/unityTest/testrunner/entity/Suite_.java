package com.unityTest.testrunner.entity;

import com.unityTest.testrunner.models.PLanguage;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Suite.class)
public abstract class Suite_ {

	public static volatile SingularAttribute<Suite, Integer> upvotes;
	public static volatile SingularAttribute<Suite, String> name;
	public static volatile SingularAttribute<Suite, PLanguage> language;
	public static volatile SingularAttribute<Suite, Integer> id;
	public static volatile SingularAttribute<Suite, String> authorId;
	public static volatile SingularAttribute<Suite, Integer> assignmentId;

	public static final String UPVOTES = "upvotes";
	public static final String NAME = "name";
	public static final String LANGUAGE = "language";
	public static final String ID = "id";
	public static final String AUTHOR_ID = "authorId";
	public static final String ASSIGNMENT_ID = "assignmentId";

}

