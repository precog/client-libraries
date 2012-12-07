package com.precog.tools.importers.jdbc

import org.specs2.mutable.Specification
import DbAnalysis._
import Datatypes._

/**
 * User: gabriel
 * Date: 12/4/12
 */
class DbAnalysisTest extends Specification {

  "declared relations" should {
    "identify one to many" in new Conn{ val dbName ="onemany"

      tblA;  tblB
      cnstrBfkA

      val metadata= conn.getMetaData
      getDeclaredRelationships(metadata,None,tA) must_== relationAtoB
      getDeclaredRelationships(metadata,None,tB) must_== relationBtoA
    }

    "identify many to many" in new Conn{ val dbName ="manymany"

      tblA; tblB; tblC
      cnstrCfkA; cnstrCfkB

      val metadata= conn.getMetaData
      getDeclaredRelationships(metadata,None,tC) must_== relationsCtoAB
      getDeclaredRelationships(metadata,None,tA) must_== relationsAtoC
      getDeclaredRelationships(metadata,None,tB) must_== relationsBtoC
    }

    "identify circular" in new Conn{ val dbName ="self"

      tblD
      cnstrDfkD

      val metadata= conn.getMetaData
      getDeclaredRelationships(metadata,None,tD) must_== relationsDtoD
    }
  }

  "inferred relations" should {
    "identify one to many" in new Conn{ val dbName ="ionemany"

      tblA;  tblB
      cnstrBfkA

      dataA; dataB

      val metadata= conn.getMetaData
      getInferredRelationships(conn,metadata,None,tA, "ID") must_== relationAtoB
      getInferredRelationships(conn,metadata,None,tB, "ID") must_== relationBtoA
    }

    "identify many to many" in new Conn{ val dbName ="imanymany"

      tblA; tblB; tblC
      cnstrCfkA; cnstrCfkB

      dataA; dataBnoA; dataC

      val metadata= conn.getMetaData
      getInferredRelationships(conn,metadata,None,tC, "ID") must_==(relationsCtoAB)
      getInferredRelationships(conn,metadata,None,tA, "ID") must_==(relationsAtoC)
      getInferredRelationships(conn,metadata,None,tB, "ID") must_==(relationsBtoC)
    }

    "identify circular" in new Conn{ val dbName ="iself"

      tblD
      cnstrDfkD
      dataD

      val metadata= conn.getMetaData
      getInferredRelationships(conn,metadata,None,tD, "ID") must_== Set(Join(pkD.columnName,fkDtoD,ExportedKey)) // can't infer both ways as relationsDtoD
    }
  }



}
