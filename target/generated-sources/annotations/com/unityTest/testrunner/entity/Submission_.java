package com.unityTest.testrunner.entity;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Submission.class)
public abstract class Submission_ {

	public static volatile ListAttribute<Submission, SubmissionFile> sourceFiles;
	public static volatile SingularAttribute<Submission, Date> submissionDate;
	public static volatile SingularAttribute<Submission, Integer> id;
	public static volatile SingularAttribute<Submission, String> authorId;
	public static volatile SingularAttribute<Submission, Integer> assignmentId;

	public static final String SOURCE_FILES = "sourceFiles";
	public static final String SUBMISSION_DATE = "submissionDate";
	public static final String ID = "id";
	public static final String AUTHOR_ID = "authorId";
	public static final String ASSIGNMENT_ID = "assignmentId";

}

