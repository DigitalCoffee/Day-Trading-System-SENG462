default: all

all: utils audit transaction http workload jar

utils:
	javac Exception/*.java
	javac Interface/*.java

# machine #b135
audit:
	javac Audit/*.java
	rmic Audit.AuditRemote

# machine #b145
transaction:
	javac Transaction/*.java
	rmic Transaction.TransactionRemote

# machine #b???
http:
	javac HTTP/*.java

# machine #b???
workload:
	javac -cp "httpcomponents-client-4.5.1/lib/*" Workload/WorkloadGenerator.java

jar:
	jar cvfe audit.jar Audit.AuditServer Audit/*.class Interface/Audit.class
	jar cvfe transaction.jar Transaction.TransactionServer Transaction/*.class Interface/*.class Exception/*.class Audit/AuditRemote_Stub.class
	jar cvfe http.jar HTTP.HTTPServer HTTP/*.class Interface/Transaction.class Transaction/TransactionRemote_Stub.class
	jar cvfe workload.jar Workload.WorkloadGenerator Workload/*.class

clean:
	rm *.jar
	rm -r Interface/*.class
	rm -r Exception/*.class
	rm -r Audit/*.class
	rm -r Transaction/*.class
	rm -r HTTP/*.class
	rm -r Workload/*.class
