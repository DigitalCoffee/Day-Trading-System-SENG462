default: all

all: interface audit transaction

interface: Interface/Audit.java
	javac Interface/Audit.java
	jar cvf audit.jar Interface/Audit.class

audit: audit.jar Audit/AuditRemote.java Audit/AuditServer.java
	javac -d Audit Audit/*.java
	rmic -classpath Audit:audit.jar AuditRemote

copy_audit: audit.jar AuditRemote_Stub.class
	cp Audit/*.class /seng/scratch/group5/
	cp AuditRemote_Stub.class /seng/scratch/group5/AuditRemote_Stub.class
	cp audit.jar /seng/scratch/group5/audit.jar

run_audit: /seng/scratch/group5/AuditServer.class /seng/scratch/group5/audit.jar
	rmiregistry 44459 &
	java -cp /seng/scratch/group5/:/seng/scratch/group5/audit.jar AuditServer

transaction: audit.jar Transaction/TransactionException.java Transaction/TransactionObjects.java Transaction/TransactionServer.java
	javac -d Transaction Transaction/*.java

copy_transaction: audit.jar AuditRemote_Stub.class
	cp Transaction/*.class /seng/scratch/group5/
	cp audit.jar /seng/scratch/group5/audit.jar
	cp AuditRemote_Stub.class /seng/scratch/group5/AuditRemote_Stub.class

run_transaction: /seng/scratch/group5/TransactionServer.class /seng/scratch/group5/audit.jar
	java -cp /seng/scratch/group5/:/seng/scratch/group5/audit.jar TransactionServer

clean:
	rm *.jar
	rm -r Interface/*.class
	rm -r Audit/*.class
	rm -r Transaction/*.class
	rm -r /seng/scratch/group5/*
