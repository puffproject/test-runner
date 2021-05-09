package com.unityTest.testrunner.entity;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(SourceFile.class)
public abstract class SourceFile_ {

	public static volatile SingularAttribute<SourceFile, String> fileName;
	public static volatile SingularAttribute<SourceFile, Long> fileSize;
	public static volatile SingularAttribute<SourceFile, String> authorId;
	public static volatile SingularAttribute<SourceFile, byte[]> content;

	public static final String FILE_NAME = "fileName";
	public static final String FILE_SIZE = "fileSize";
	public static final String AUTHOR_ID = "authorId";
	public static final String CONTENT = "content";

}

