package com.precog.tools.importers.jdbc

import org.specs2.mutable.Specification
import com.precog.tools.importers.jdbc.DbAnalysis._

/**
 * User: gabriel
 * Date: 12/4/12
 */
class DbAnalysisTest extends Specification {

  "declared relations" should {
    "identify one to many" in new Conn("onemany"){

      tblA;  tblB
      cnstrBfkA

      val metadata= conn.getMetaData
      getDeclaredRelationships(metadata,null,tA) must_== relationAtoB
      getDeclaredRelationships(metadata,null,tB) must_== relationBtoA
    }

    "identify many to many" in new Conn("manymany") {

      tblA; tblB; tblC
      cnstrCfkA; cnstrCfkB

      val metadata= conn.getMetaData
      getDeclaredRelationships(metadata,null,tC) must_== relationsCtoAB
      getDeclaredRelationships(metadata,null,tA) must_== relationsAtoC
      getDeclaredRelationships(metadata,null,tB) must_== relationsBtoC
    }

    "identify circular" in new Conn("self") {

      tblD
      cnstrDfkD

      val metadata= conn.getMetaData
      getDeclaredRelationships(metadata,null,tD) must_== relationsDtoD
    }
  }

  "inferred relations" should {
    "identify one to many" in new Conn("ionemany"){

      tblA;  tblB
      cnstrBfkA

      dataA; dataB

      val metadata= conn.getMetaData
      getInferredRelationships(conn,metadata,null,tA, "ID") must_== relationAtoB
      getInferredRelationships(conn,metadata,null,tB, "ID") must_== relationBtoA
    }

    "identify many to many" in new Conn("imanymany") {

      tblA; tblB; tblC
      cnstrCfkA; cnstrCfkB

      dataA; dataBnoA; dataC

      val metadata= conn.getMetaData
      getInferredRelationships(conn,metadata,null,tC, "ID") must_==(relationsCtoAB)
      getInferredRelationships(conn,metadata,null,tA, "ID") must_==(relationsAtoC)
      getInferredRelationships(conn,metadata,null,tB, "ID") must_==(relationsBtoC)
    }

    "identify circular" in new Conn("iself") {

      tblD
      cnstrDfkD
      dataD

      val metadata= conn.getMetaData
      getInferredRelationships(conn,metadata,null,tD, "ID") must_== Set(Join(pkD.columnName,fkDtoD,exported)) // can't infer both ways as relationsDtoD
    }
  }



}
