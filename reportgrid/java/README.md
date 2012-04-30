# Maven Setup

To use the Java client with Maven, add the following repository to your POM:

    <repositories>
      ...
      <repository>
        <id>reportgrid-maven</id>
        <url>http://nexus.reportgrid.com/content/repositories/public-releases</url>
      </repository>
    </repositories>

Then add your dependency:

    <dependency>
      <groupId>com.reportgrid</groupId>
      <artifactId>reportgrid-java-client</artifactId>
      <version>1.0.6</version>
    </dependency>

