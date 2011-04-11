# Introduction

A Scala client library for the ReportGrid API.

The client library depends on Rosetta Json to support three different Scala Json libraries:

 * BlueEyes Json
 * Lift Json
 * Dispatch Json

ReportGrid API facades exist for each of the Json libraries:

 * `com.reportgrid.api.blueeyes.ReportGrid`
 * `com.reportgrid.api.lift.ReportGrid`
 * `com.reportgrid.api.dispatch.ReportGrid`

# Getting Started

To create a facade, simply specify the token API as the only constructor parameter:

    import com.reportgrid.api.blueeyes.ReportGrid
    import com.reportgrid.api._

    val api = new ReportGrid(tokenId)

## Tracking Data

    api.track(
      path       = "/customers/jdoe"
      event      = "transaction",
      properties = JObject(
        JField("type",      JString("purchase")) ::
        JField("amount",    JInt(1095)) ::
        JField("location",  JString("USA_CO_Boulder")) ::
        JField("gender",    JString("male")) ::
        Nil
      ),
      rollup     = true
    )

## Virtual File System

    val children = api.list("/customers/")

## Property Values

    val values = api.valuesOf(".transaction.location").from("/customers/")

## Counts & Time Series for Properties

    val count = api.select(Count).of(".transaction.location").from("/customers/")

    val series = api.select(Hour(start, end)).of(".transaction.location").from("/customers/")

## Counts & Time Series for Property Values

    val count = api.select(Count).from("/customers/").where {
      ".transaction.gender" === "male"
    }

    val series = api.select(Hour(start, end)).from("/customers/").where {
      ".transaction.gender" === "male" &&
      ".transaction.type"   === "purchase"
    }

# Customization

If you wish to provide your own Json library and Http client, you can do so using `ReportGridGeneric`. You will need to supply an implementation of `JsonImplementation` from Rosetta Json, and an implementation of `HttpClient[String]`.