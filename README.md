---
page_type: sample
languages:
- java
products:
- azure
- azure-cosmos-db
description: "This Java reference sample shows how CosmosDB Pagination can be achieved with ContinuationToken"
---

# Achieving CosmosDB Pagination with ContinuationToken

<!-- 
Guidelines on README format: https://review.docs.microsoft.com/help/onboard/admin/samples/concepts/readme-template?branch=master

Guidance on onboarding samples to docs.microsoft.com/samples: https://review.docs.microsoft.com/help/onboard/admin/samples/process/onboarding?branch=master

Taxonomies for products and languages: https://review.docs.microsoft.com/new-hope/information-architecture/metadata/taxonomies?branch=master
-->

Give a short description for your sample here. What does it do and why is it important?

## Contents

Outline the file contents of the repository. It helps users navigate the codebase, build configuration and any related assets.

| File/folder       | Description                                |
|-------------------|--------------------------------------------|
| `src`             | Sample source code.                        |
| `.gitignore`      | Define what to ignore at commit time.      |
| `CHANGELOG.md`    | List of changes to the sample.             |
| `CONTRIBUTING.md` | Guidelines for contributing to the sample. |
| `README.md`       | This README file.                          |
| `LICENSE`         | The license for the sample.                |

## Prerequisites

Outline the required components and tools that a user might need to have on their machine in order to run the sample. This can be anything from frameworks, SDKs, OS versions or IDE releases.

## Setup

git clone https://github.com/Azure-Samples/azure-cosmosdb-java-pagination.git 

cd azure-cosmosdb-java-pagination 

Update AccountSettings.java with cosmosdb hostname and key 


## Running the sample

Run Main.Java 

This method returns a map with nextContinuationtoken and prevContinuationToken with resultsets Which can be used in UI
QueryPageByPage();

This method shows how to querying a Document with a list saved in a Collection into Cache which can be used sliced in UI code for pagination
This example shows EHCache, however for centralized management rediscache can also be used
executeSimpleQueryWithList();

## Key concepts

CosmosDB ContinuationToken : Pagination of records can then be retrieved by supplying the continuation token in subsequent calls. The coordination between the CosmosDB service and the client is taken care of behind the scenes by the SDKs.
## Contributing

This project welcomes contributions and suggestions.  Most contributions require you to agree to a
Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us
the rights to use your contribution. For details, visit https://cla.opensource.microsoft.com.

When you submit a pull request, a CLA bot will automatically determine whether you need to provide
a CLA and decorate the PR appropriately (e.g., status check, comment). Simply follow the instructions
provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or
contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.
