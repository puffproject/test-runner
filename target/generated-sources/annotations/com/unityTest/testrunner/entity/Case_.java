package com.unityTest.testrunner.entity;

import com.unityTest.testrunner.models.PLanguage;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Case.class)
public abstract class Case_ {

	public static volatile SingularAttribute<Case, Suite> suite;
	public static volatile SingularAttribute<Case, String> code;
	public static volatile SingularAttribute<Case, Integer> upvotes;
	public static volatile SingularAttribute<Case, String> functionName;
	public static volatile SingularAttribute<Case, String> description;
	public static volatile SingularAttribute<Case, PLanguage> language;
	public static volatile SingularAttribute<Case, Integer> id;
	public static volatile SingularAttribute<Case, String> authorId;
	public static volatile SingularAttribute<Case, Integer> runCount;
	public static volatile SingularAttribute<Case, Integer> passCount;

	public static final String SUITE = "suite";
	public static final String CODE = "code";
	public static final String UPVOTES = "upvotes";
	public static final String FUNCTION_NAME = "functionName";
	public static final String DESCRIPTION = "description";
	public static final String LANGUAGE = "language";
	public static final String ID = "id";
	public static final String AUTHOR_ID = "authorId";
	public static final String RUN_COUNT = "runCount";
	public static final String PASS_COUNT = "passCount";

}

