#
#
# DB configs
# DB Type can be Hive, SQLite, SQLServer
dbType=SQLServer
# ConnectionType can be SQLDataSource, UrlAuth, url
connectionType=SQLDataSource
url=192.168.57.101
# optional
classForName=org.apache.hive.jdbc.HiveDriver
# user and password are only needed for SQLDataSource and urlAuth
user=Regateiro
password=123456
# dbName is only needed for SQLDataSource
dbName=Northwind
#
#
# Policy Server Configs
urlPS=192.168.57.101
dbNamePS=PolicyServer2
userPS=regateiro
passwordPS=123456
#
#
# Boolean features
# Whether to use custom transactions
r4nTrans=false
# Whether to use custom ResultSets
r4nIAM=false
# Whether to use custom CallableStatements
r4nSP=false
# Whether to map Column Names to indexes using the PS
r4nColI=false
#
# FT
ft=NoOp
#ftLocation=
#
# CH
ch=Local
#chLocation=
#

