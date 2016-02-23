default: all

all: interface audit transaction

interface: Interface/Audit.java
	javac Exception/*.java
	javac Interface/*.java
	jar cvf utils.jar Interface/*.class Exception/*.class

audit: utils.jar Audit/AuditRemote.java Audit/AuditServer.java
	javac -d Audit Audit/*.java
	rmic -classpath Audit:utils.jar AuditRemote

copy_audit: utils.jar AuditRemote_Stub.class TransactionRemote_Stub.class
	cp Audit/*.class /seng/scratch/group5/
	cp AuditRemote_Stub.class /seng/scratch/group5/AuditRemote_Stub.class
	cp TransactionRemote_Stub.class /seng/scratch/group5/TransactionRemote_Stub.class
	cp utils.jar /seng/scratch/group5/utils.jar

run_audit: /seng/scratch/group5/AuditServer.class /seng/scratch/group5/utils.jar
	rmiregistry 44459 &
	java -cp /seng/scratch/group5/:/seng/scratch/group5/utils.jar AuditServer

transaction: utils.jar Transaction/TransactionObjects.java Transaction/TransactionRemote.java Transaction/TransactionServer.java
	javac -d Transaction Transaction/*.java
	rmic -classpath Transaction:utils.jar TransactionRemote

copy_transaction: utils.jar AuditRemote_Stub.class TransactionRemote_Stub.class
	cp Transaction/*.class /seng/scratch/group5/
	cp AuditRemote_Stub.class /seng/scratch/group5/AuditRemote_Stub.class
	cp TransactionRemote_Stub.class /seng/scratch/group5/TransactionRemote_Stub.class
	cp utils.jar /seng/scratch/group5/utils.jar

run_transaction: /seng/scratch/group5/TransactionServer.class /seng/scratch/group5/utils.jar
	rmiregistry 44459 &
	java -cp /seng/scratch/group5/:/seng/scratch/group5/utils.jar TransactionServer

clean:
	rm *.jar
	rm *.class
	rm -r Interface/*.class
	rm -r Audit/*.class
	rm -r Transaction/*.class
	rm -r /seng/scratch/group5/*
