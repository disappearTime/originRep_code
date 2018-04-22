app_template为多模块项目模板

项目主框架集成了spring4.2.5、velocity1.7、mybatis

主pom.xml只维护所有jar包以及版本号
其他模块类的pom.xml不进行版本号的维护

api模块是对外部的一些的接口和接口所需要的参数类

api_impl模块是对api模块的具体实现，目前为dubbo服务实现类，web工程，主要提供dubbo服务（打成war包部署）

common模块主要放项目的一些公共变量以及公共的工具，尽可能的减少外部依赖

dao模块为操作数据库模块，连接数据库的接口都在这个模块里，mysql实现（最佳实践：不同的数据库放在不同的子package下）

domain模块放所有数据库操作的实体类

job模块里面是所有的job类、定时任务类

web模块是对外提供服务的模块（打成war包部署）

maven settings文件：

<?xml version="1.0" encoding="UTF-8"?>
  <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
   <servers>
       <server>
       <id>nexus-releases</id>
       <username>admin</username>
       <password>admin123</password>
     </server>
     <server>
       <id>nexus-snapshots</id>
       <username>admin</username>
       <password>admin123</password>
     </server>
   </servers>

   <mirrors>
     <mirror>
       <id>nexus-releases</id>
       <mirrorOf>*</mirrorOf>
       <url>http://192.168.1.242:8081/nexus/content/groups/public</url>
     </mirror>
     <mirror>
       <id>nexus-snapshots</id>
       <mirrorOf>*</mirrorOf>
       <url>http://192.168.1.242:8081/nexus/content/groups/public-snapshots</url>
     </mirror>
   </mirrors>

   <profiles>
    <profile>
       <id>nexus</id>
       <repositories>
         <repository>
           <id>nexus-releases</id>
           <url>http://nexus-releases</url>
           <releases><enabled>true</enabled></releases>
           <snapshots><enabled>true</enabled></snapshots>
         </repository>
         <repository>
           <id>nexus-snapshots</id>
           <url>http://nexus-snapshots</url>
           <releases><enabled>true</enabled></releases>
           <snapshots><enabled>true</enabled></snapshots>
         </repository>
       </repositories>
       <pluginRepositories>
          <pluginRepository>
                 <id>nexus-releases</id>
                  <url>http://nexus-releases</url>
                  <releases><enabled>true</enabled></releases>
                  <snapshots><enabled>true</enabled></snapshots>
                </pluginRepository>
                <pluginRepository>
                  <id>nexus-snapshots</id>
                   <url>http://nexus-snapshots</url>
                 <releases><enabled>true</enabled></releases>
                  <snapshots><enabled>true</enabled></snapshots>
              </pluginRepository>
          </pluginRepositories>
     </profile>
   </profiles>

   <activeProfiles>
       <activeProfile>nexus</activeProfile>
   </activeProfiles>

 </settings>


