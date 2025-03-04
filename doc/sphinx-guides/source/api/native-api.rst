Native API
==========

Dataverse 4 exposes most of its GUI functionality via a REST-based API. This section describes that functionality. Most API endpoints require an API token that can be passed as the ``X-Dataverse-key`` HTTP header or in the URL as the ``key`` query parameter.

.. note:: |CORS| Some API endpoint allow CORS_ (cross-origin resource sharing), which makes them usable from scripts runing in web browsers. These endpoints are marked with a *CORS* badge.

.. note:: Bash environment variables shown below. The idea is that you can "export" these environment variables before copying and pasting the commands that use them. For example, you can set ``$SERVER_URL`` by running ``export SERVER_URL="https://demo.dataverse.org"`` in your Bash shell. To check if the environment variable was set properly, you can "echo" it (e.g. ``echo $SERVER_URL``). See also :ref:`curl-examples-and-environment-variables`.

.. _CORS: https://www.w3.org/TR/cors/

.. warning:: Dataverse 4's API is versioned at the URI - all API calls may include the version number like so: ``http://server-address/api/v1/...``. Omitting the ``v1`` part would default to the latest API version (currently 1). When writing scripts/applications that will be used for a long time, make sure to specify the API version, so they don't break when the API is upgraded.

.. contents:: |toctitle|
    :local:

Dataverses
----------

.. _create-dataverse-api:

Create a Dataverse
~~~~~~~~~~~~~~~~~~

A dataverse is a container for datasets and other dataverses as explained in the :doc:`/user/dataverse-management` section of the User Guide.

The steps for creating a dataverse are:

- Prepare a JSON file containing the name, description, etc, of the dataverse you'd like to create.
- Figure out the alias or database id of the "parent" dataverse into which you will be creating your new dataverse.
- Execute a curl command or equivalent.

Download :download:`dataverse-complete.json <../_static/api/dataverse-complete.json>` file and modify it to suit your needs. The fields ``name``, ``alias``, and ``dataverseContacts`` are required. The controlled vocabulary for ``dataverseType`` is the following:

- ``DEPARTMENT``
- ``JOURNALS``
- ``LABORATORY``
- ``ORGANIZATIONS_INSTITUTIONS``
- ``RESEARCHERS``
- ``RESEARCH_GROUP``
- ``RESEARCH_PROJECTS``
- ``TEACHING_COURSES``
- ``UNCATEGORIZED``

.. literalinclude:: ../_static/api/dataverse-complete.json

The curl command below assumes you have kept the name "dataverse-complete.json" and that this file is in your current working directory.

Next you need to figure out the alias or database id of the "parent" dataverse into which you will be creating your new dataverse. Out of the box the top level dataverse has an alias of "root" and a database id of "1" but your installation may vary. The easiest way to determine the alias of your root dataverse is to click "Advanced Search" and look at the URL. You may also choose a parent under the root.

.. note:: See :ref:`curl-examples-and-environment-variables` if you are unfamiliar with the use of ``export`` below.

.. code-block:: bash

  export API_TOKEN=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
  export PARENT=root
  export SERVER_URL=https://demo.dataverse.org
  
  curl -H X-Dataverse-key:$API_TOKEN -X POST $SERVER_URL/api/dataverses/$PARENT --upload-file dataverse-complete.json

The fully expanded example above (without environment variables) looks like this:

.. code-block:: bash

  curl -H X-Dataverse-key:xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx -X POST https://demo.dataverse.org/api/dataverses/root --upload-file dataverse-complete.json

You should expect a 201 ("CREATED") response and JSON indicating the database id that has been assigned to your newly created dataverse.

.. _view-dataverse:

View a Dataverse
~~~~~~~~~~~~~~~~

|CORS| View data about the dataverse identified by ``$id``. ``$id`` can be the id number of the dataverse, its identifier (a.k.a. alias), or the special value ``:root`` for the root dataverse.

``curl $SERVER_URL/api/dataverses/$id``

Delete a Dataverse
~~~~~~~~~~~~~~~~~~

In order to delete a dataverse you must first delete or move all of its contents elsewhere.

Deletes the dataverse whose ID is given:

``curl -H "X-Dataverse-key:$API_TOKEN" -X DELETE $SERVER_URL/api/dataverses/$id``

.. _show-contents-of-a-dataverse-api:

Show Contents of a Dataverse
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

|CORS| Lists all the dataverses and datasets directly under a dataverse (direct children only). You must specify the "alias" of a dataverse or its database id. If you specify your API token and have access, unpublished dataverses and datasets will be included in the listing.

.. note:: See :ref:`curl-examples-and-environment-variables` if you are unfamiliar with the use of ``export`` below.

.. code-block:: bash

  export API_TOKEN=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
  export ALIAS=root
  export SERVER_URL=https://demo.dataverse.org
  
  curl -H X-Dataverse-key:$API_TOKEN $SERVER_URL/api/dataverses/$ALIAS/contents

The fully expanded example above (without environment variables) looks like this:

.. code-block:: bash

  curl -H X-Dataverse-key:xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx https://demo.dataverse.org/api/dataverses/root/contents

Report the data (file) size of a Dataverse
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Shows the combined size in bytes of all the files uploaded into the dataverse ``id``. ::

``curl -H "X-Dataverse-key:$API_TOKEN" http://$SERVER_URL/api/dataverses/$id/storagesize``

Both published and unpublished files will be counted, in the dataverse specified, and in all its sub-dataverses, recursively. 
By default, only the archival files are counted - i.e., the files uploaded by users (plus the tab-delimited versions generated for tabular data files on ingest). If the optional argument ``includeCached=true`` is specified, the API will also add the sizes of all the extra files generated and cached by Dataverse - the resized thumbnail versions for image files, the metadata exports for published datasets, etc. 


List Roles Defined in a Dataverse
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

All the roles defined directly in the dataverse identified by ``id``::

  GET http://$SERVER/api/dataverses/$id/roles?key=$apiKey

List Facets Configured for a Dataverse
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

|CORS| List all the facets for a given dataverse ``id``. ::

  GET http://$SERVER/api/dataverses/$id/facets?key=$apiKey

Set Facets for a Dataverse
~~~~~~~~~~~~~~~~~~~~~~~~~~

Assign search facets for a given dataverse with alias ``$alias``

``curl -H "X-Dataverse-key: $apiKey" -X POST http://$server/api/dataverses/$alias/facets --upload-file facets.json``

Where ``facets.json`` contains a JSON encoded list of metadata keys (e.g. ``["authorName","authorAffiliation"]``).

Create a New Role in a Dataverse
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Creates a new role under dataverse ``id``. Needs a json file with the role description::

  POST http://$SERVER/api/dataverses/$id/roles?key=$apiKey
  
POSTed JSON example::

  {
    "alias": "sys1",
    "name": “Restricted System Role”,
    "description": “A person who may only add datasets.”,
    "permissions": [
      "AddDataset"
    ]
  } 

.. _list-role-assignments-on-a-dataverse-api:

List Role Assignments in a Dataverse
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

List all the role assignments at the given dataverse::

  GET http://$SERVER/api/dataverses/$id/assignments?key=$apiKey
  
Assign Default Role to User Creating a Dataset in a Dataverse
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Assign a default role to a user creating a dataset in a dataverse ``id`` where ``roleAlias`` is the database alias of the role to be assigned::

  PUT http://$SERVER/api/dataverses/$id/defaultContributorRole/$roleAlias?key=$apiKey
  
Note: You may use "none" as the ``roleAlias``. This will prevent a user who creates a dataset from having any role on that dataset. It is not recommended for dataverses with human contributors.

.. _assign-role-on-a-dataverse-api:

Assign a New Role on a Dataverse
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Assigns a new role, based on the POSTed JSON. ::

  POST http://$SERVER/api/dataverses/$id/assignments?key=$apiKey

POSTed JSON example::

  {
    "assignee": "@uma",
    "role": "curator"
  }

.. _revoke-role-on-a-dataverse-api:

Delete Role Assignment from a Dataverse
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Delete the assignment whose id is ``$id``::

  DELETE http://$SERVER/api/dataverses/$id/assignments/$id?key=$apiKey

List Metadata Blocks Defined on a Dataverse
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

|CORS| Get the metadata blocks defined on a dataverse which determine which field are available to authors when they create and edit datasets within that dataverse. This feature is described under "General Information" in the :doc:`/user/dataverse-management` section of the User Guide.

Please note that an API token is only required if the dataverse has not been published.

.. note:: See :ref:`curl-examples-and-environment-variables` if you are unfamiliar with the use of ``export`` below.

.. code-block:: bash

  export API_TOKEN=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
  export ALIAS=root
  export SERVER_URL=https://demo.dataverse.org

  curl -H X-Dataverse-key:$API_TOKEN $SERVER_URL/api/dataverses/$ALIAS/metadatablocks

The fully expanded example above (without environment variables) looks like this:

.. code-block:: bash

  curl -H X-Dataverse-key:xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx https://demo.dataverse.org/api/dataverses/root/metadatablocks

Define Metadata Blocks for a Dataverse
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

You can define the metadata blocks available to authors within a dataverse.

The metadata blocks that are available with a default installation of Dataverse are in :download:`define-metadatablocks.json <../_static/api/define-metadatablocks.json>` (also shown below) and you should download this file and edit it to meet your needs. Please note that the "citation" metadata block is required. You must have "EditDataverse" permission on the dataverse.

.. literalinclude:: ../_static/api/define-metadatablocks.json

.. note:: See :ref:`curl-examples-and-environment-variables` if you are unfamiliar with the use of ``export`` below.

.. code-block:: bash

  export API_TOKEN=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
  export ALIAS=root
  export SERVER_URL=https://demo.dataverse.org
  
  curl -H X-Dataverse-key:$API_TOKEN -X POST -H \"Content-type:application/json\" --upload-file define-metadatablocks.json $SERVER_URL/api/dataverses/$ALIAS/metadatablocks

The fully expanded example above (without environment variables) looks like this:

.. code-block:: bash

  curl -H X-Dataverse-key:xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx -X POST -H "Content-type:application/json" --upload-file define-metadatablocks.json https://demo.dataverse.org/api/dataverses/root/metadatablocks

Determine if a Dataverse Inherits Its Metadata Blocks from Its Parent
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Get whether the dataverse is a metadata block root, or does it uses its parent blocks::

  GET http://$SERVER/api/dataverses/$id/metadatablocks/isRoot?key=$apiKey

Configure a Dataverse to Inherit Its Metadata Blocks from Its Parent
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Set whether the dataverse is a metadata block root, or does it uses its parent blocks. Possible
values are ``true`` and ``false`` (both are valid JSON expressions). ::

  PUT http://$SERVER/api/dataverses/$id/metadatablocks/isRoot?key=$apiKey

.. note:: Previous endpoints ``GET http://$SERVER/api/dataverses/$id/metadatablocks/:isRoot?key=$apiKey`` and ``POST http://$SERVER/api/dataverses/$id/metadatablocks/:isRoot?key=$apiKey`` are deprecated, but supported.


.. _create-dataset-command: 

Create a Dataset in a Dataverse
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

A dataset is a container for files as explained in the :doc:`/user/dataset-management` section of the User Guide.

To create a dataset, you must supply a JSON file that contains at least the following required metadata fields:

- Title
- Author
- Description
- Subject

As a starting point, you can download :download:`dataset-finch1.json <../../../../scripts/search/tests/data/dataset-finch1.json>` and modify it to meet your needs. (In addition to this minimal example, you can download :download:`dataset-create-new-all-default-fields.json <../../../../scripts/api/data/dataset-create-new-all-default-fields.json>` which populates all of the metadata fields that ship with Dataverse.)

The curl command below assumes you have kept the name "dataset-finch1.json" and that this file is in your current working directory.

Next you need to figure out the alias or database id of the "parent" dataverse into which you will be creating your new dataset. Out of the box the top level dataverse has an alias of "root" and a database id of "1" but your installation may vary. The easiest way to determine the alias of your root dataverse is to click "Advanced Search" and look at the URL. You may also choose a parent dataverse under the root dataverse.

.. note:: See :ref:`curl-examples-and-environment-variables` if you are unfamiliar with the use of ``export`` below.

.. code-block:: bash

  export API_TOKEN=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
  export PARENT=root
  export SERVER_URL=https://demo.dataverse.org

  curl -H X-Dataverse-key:$API_TOKEN -X POST $SERVER_URL/api/dataverses/$PARENT/datasets --upload-file dataset-finch1.json

The fully expanded example above (without the environment variables) looks like this:

.. code-block:: bash

  curl -H X-Dataverse-key:xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx -X POST https://demo.dataverse.org/api/dataverses/root/datasets --upload-file dataset-finch1.json

You should expect a 201 ("CREATED") response and JSON indicating the database ID and Persistent ID (PID such as DOI or Handle) that has been assigned to your newly created dataset.

Import a Dataset into a Dataverse
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. note:: This action requires a Dataverse account with super-user permissions.

To import a dataset with an existing persistent identifier (PID), the dataset's metadata should be prepared in Dataverse's native JSON format. The PID is provided as a parameter at the URL. The following line imports a dataset with the PID ``PERSISTENT_IDENTIFIER`` to Dataverse, and then releases it::

  curl -H "X-Dataverse-key: $API_TOKEN" -X POST $SERVER_URL/api/dataverses/$DV_ALIAS/datasets/:import?pid=$PERSISTENT_IDENTIFIER&release=yes --upload-file dataset.json

The ``pid`` parameter holds a persistent identifier (such as a DOI or Handle). The import will fail if no PID is provided, or if the provided PID fails validation.

The optional ``release`` parameter tells Dataverse to immediately publish the dataset. If the parameter is changed to ``no``, the imported dataset will remain in ``DRAFT`` status.

The JSON format is the same as that supported by the native API's :ref:`create dataset command<create-dataset-command>`, although it also allows packages.  For example:

.. literalinclude:: ../../../../scripts/api/data/dataset-package-files.json

Before calling the API, make sure the data files referenced by the ``POST``\ ed JSON are placed in the dataset directory with filenames matching their specified storage identifiers. In installations using POSIX storage, these files must be made readable by GlassFish.

.. tip:: If possible, it's best to avoid spaces and special characters in the storage identifier in order to avoid potential portability problems. The storage identifier corresponds with the filesystem name (or bucket identifier) of the data file, so these characters may cause unpredictability with filesystem tools.

.. warning:: 
  
  * This API does not cover staging files (with correct contents, checksums, sizes, etc.) in the corresponding places in the Dataverse filestore.
  * This API endpoint does not support importing *files'* persistent identifiers.
  * A Dataverse server can import datasets with a valid PID that uses a different protocol or authority than said server is configured for. However, the server will not update the PID metadata on subsequent update and publish actions.


Import a Dataset into a Dataverse with a DDI file
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. note:: This action requires a Dataverse account with super-user permissions.

To import a dataset with an existing persistent identifier (PID), you have to provide the PID as a parameter at the URL. The following line imports a dataset with the PID ``PERSISTENT_IDENTIFIER`` to Dataverse, and then releases it::

  curl -H "X-Dataverse-key: $API_TOKEN" -X POST $SERVER_URL/api/dataverses/$DV_ALIAS/datasets/:importddi?pid=$PERSISTENT_IDENTIFIER&release=yes --upload-file ddi_dataset.xml

The optional ``pid`` parameter holds a persistent identifier (such as a DOI or Handle). The import will fail if the provided PID fails validation.

The optional ``release`` parameter tells Dataverse to immediately publish the dataset. If the parameter is changed to ``no``, the imported dataset will remain in ``DRAFT`` status.

The file is a DDI xml file.

.. warning::

  * This API does not handle files related to the DDI file.
  * A Dataverse server can import datasets with a valid PID that uses a different protocol or authority than said server is configured for. However, the server will not update the PID metadata on subsequent update and publish actions.

.. _publish-dataverse-api:

Publish a Dataverse
~~~~~~~~~~~~~~~~~~~

In order to publish a dataverse, you must know either its "alias" (which the GUI calls an "identifier") or its database ID.

.. note:: See :ref:`curl-examples-and-environment-variables` if you are unfamiliar with the use of ``export`` below.

.. code-block:: bash

  export API_TOKEN=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
  export ALIAS=root
  export SERVER_URL=https://demo.dataverse.org

  curl -H X-Dataverse-key:$API_TOKEN -X POST $SERVER_URL/api/dataverses/$ALIAS/actions/:publish

The fully expanded example above (without environment variables) looks like this:

.. code-block:: bash

  curl -H X-Dataverse-key:xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx -X POST https://demo.dataverse.org/api/dataverses/root/actions/:publish

You should expect a 200 ("OK") response and JSON output.

Datasets
--------

**Note** Creation of new datasets is done with a ``POST`` onto dataverses. See Dataverses_ section.

**Note** In all commands below, dataset versions can be referred to as:

* ``:draft``  the draft version, if any
* ``:latest`` either a draft (if exists) or the latest published version.
* ``:latest-published`` the latest published version
* ``x.y`` a specific version, where ``x`` is the major version number and ``y`` is the minor version number.
* ``x`` same as ``x.0``

Get JSON Representation of a Dataset
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. note:: Datasets can be accessed using persistent identifiers. This is done by passing the constant ``:persistentId`` where the numeric id of the dataset is expected, and then passing the actual persistent id as a query parameter with the name ``persistentId``.

  Example: Getting the dataset whose DOI is *10.5072/FK2/J8SJZB* ::

    curl http://$SERVER/api/datasets/:persistentId/?persistentId=doi:10.5072/FK2/J8SJZB

  fully expanded::

    curl http://localhost:8080/api/datasets/:persistentId/?persistentId=doi:10.5072/FK2/J8SJZB

  Getting its draft version::

    curl http://$SERVER/api/datasets/:persistentId/versions/:draft?persistentId=doi:10.5072/FK2/J8SJZB

  fully expanded::

    curl http://localhost:8080/api/datasets/:persistentId/versions/:draft?persistentId=doi:10.5072/FK2/J8SJZB


|CORS| Show the dataset whose id is passed::

  GET http://$SERVER/api/datasets/$id?key=$apiKey

List Versions of a Dataset
~~~~~~~~~~~~~~~~~~~~~~~~~~

|CORS| List versions of the dataset::

  GET http://$SERVER/api/datasets/$id/versions?key=$apiKey

Get Version of a Dataset
~~~~~~~~~~~~~~~~~~~~~~~~

|CORS| Show a version of the dataset. The Dataset also include any metadata blocks the data might have::

  GET http://$SERVER/api/datasets/$id/versions/$versionNumber?key=$apiKey

.. _export-dataset-metadata-api:

Export Metadata of a Dataset in Various Formats
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

|CORS| Export the metadata of the current published version of a dataset in various formats see Note below::

    GET http://$SERVER/api/datasets/export?exporter=ddi&persistentId=$persistentId

.. note:: Supported exporters (export formats) are ``ddi``, ``oai_ddi``, ``dcterms``, ``oai_dc``, ``schema.org`` , ``OAI_ORE`` , ``Datacite``, ``oai_datacite`` and ``dataverse_json``. Descriptive names can be found under :ref:`metadata-export-formats` in the User Guide.


Schema.org JSON-LD
^^^^^^^^^^^^^^^^^^

Please note that the ``schema.org`` format has changed in backwards-incompatible ways after Dataverse 4.9.4:

- "description" was a single string and now it is an array of strings.
- "citation" was an array of strings and now it is an array of objects.

Both forms are valid according to Google's Structured Data Testing Tool at https://search.google.com/structured-data/testing-tool . (This tool will report "The property affiliation is not recognized by Google for an object of type Thing" and this known issue is being tracked at https://github.com/IQSS/dataverse/issues/5029 .) Schema.org JSON-LD is an evolving standard that permits a great deal of flexibility. For example, https://schema.org/docs/gs.html#schemaorg_expected indicates that even when objects are expected, it's ok to just use text. As with all metadata export formats, we will try to keep the Schema.org JSON-LD format Dataverse emits backward-compatible to made integrations more stable, despite the flexibility that's afforded by the standard.

List Files in a Dataset
~~~~~~~~~~~~~~~~~~~~~~~

|CORS| Lists all the file metadata, for the given dataset and version::

  GET http://$SERVER/api/datasets/$id/versions/$versionId/files?key=$apiKey

List All Metadata Blocks for a Dataset
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

|CORS| Lists all the metadata blocks and their content, for the given dataset and version::

  GET http://$SERVER/api/datasets/$id/versions/$versionId/metadata?key=$apiKey

List Single Metadata Block for a Dataset
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

|CORS| Lists the metadata block block named `blockname`, for the given dataset and version::

  GET http://$SERVER/api/datasets/$id/versions/$versionId/metadata/$blockname?key=$apiKey

Update Metadata For a Dataset
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Updates the metadata for a dataset. If a draft of the dataset already exists, the metadata of that draft is overwritten; otherwise, a new draft is created with this metadata.

You must download a JSON representation of the dataset, edit the JSON you download, and then send the updated JSON to the Dataverse server.

For example, after making your edits, your JSON file might look like :download:`dataset-update-metadata.json <../_static/api/dataset-update-metadata.json>` which you would send to Dataverse like this::

    curl -H "X-Dataverse-key: $API_TOKEN" -X PUT $SERVER_URL/api/datasets/:persistentId/versions/:draft?persistentId=$PID --upload-file dataset-update-metadata.json

Note that in the example JSON file above, there is a single JSON object with ``metadataBlocks`` as a key. When you download a representation of your dataset in JSON format, the ``metadataBlocks`` object you need is nested inside another object called ``json``. To extract just the ``metadataBlocks`` key when downloading a JSON representation, you can use a tool such as ``jq`` like this::

    curl -H "X-Dataverse-key: $API_TOKEN" $SERVER_URL/api/datasets/:persistentId/versions/:latest?persistentId=$PID | jq '.data | {metadataBlocks: .metadataBlocks}' > dataset-update-metadata.json

Now that the resulting JSON file only contains the ``metadataBlocks`` key, you can edit the JSON such as with ``vi`` in the example below::

    vi dataset-update-metadata.json

Now that you've made edits to the metadata in your JSON file, you can send it to Dataverse as described above.

Edit Dataset Metadata
~~~~~~~~~~~~~~~~~~~~~

Alternatively to replacing an entire dataset version with its JSON representation you may add data to dataset fields that are blank or accept multiple values with the following ::

    curl -H "X-Dataverse-key: $API_TOKEN" -X PUT $SERVER_URL/api/datasets/:persistentId/editMetadata/?persistentId=$PID --upload-file dataset-add-metadata.json    

You may also replace existing metadata in dataset fields with the following (adding the parameter replace=true)   ::

    curl -H "X-Dataverse-key: $API_TOKEN" -X PUT $SERVER_URL/api/datasets/:persistentId/editMetadata?persistentId=$PID&replace=true --upload-file dataset-update-metadata.json
    
For these edits your JSON file need only include those dataset fields which you would like to edit. A sample JSON file may be downloaded here: :download:`dataset-edit-metadata-sample.json <../_static/api/dataset-edit-metadata-sample.json>` 

Delete Dataset Metadata
~~~~~~~~~~~~~~~~~~~~~~~

You may delete some of the metadata of a dataset version by supplying a file with a JSON representation of dataset fields that you would like to delete with the following ::

    curl -H "X-Dataverse-key: $API_TOKEN" -X PUT $SERVER_URL/api/datasets/:persistentId/deleteMetadata/?persistentId=$PID --upload-file dataset-delete-author-metadata.json    
    
For these deletes your JSON file must include an exact match of those dataset fields which you would like to delete. A sample JSON file may be downloaded here: :download:`dataset-delete-author-metadata.json <../_static/api/dataset-delete-author-metadata.json>` 


.. _publish-dataset-api:

Publish a Dataset
~~~~~~~~~~~~~~~~~

When publishing a dataset it's good to be aware of Dataverse's versioning system, which is described in the :doc:`/user/dataset-management` section of the User Guide.

If this is the first version of the dataset, its version number will be set to ``1.0``. Otherwise, the new dataset version number is determined by the most recent version number and the ``type`` parameter. Passing ``type=minor`` increases the minor version number (2.3 is updated to 2.4). Passing ``type=major`` increases the major version number (2.3 is updated to 3.0). (Superusers can pass ``type=updatecurrent`` to update metadata without changing the version number.)

.. note:: See :ref:`curl-examples-and-environment-variables` if you are unfamiliar with the use of ``export`` below.

.. code-block:: bash

  export API_TOKEN=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
  export SERVER_URL=https://demo.dataverse.org
  export PERSISTENT_ID=doi:10.5072/FK2/J8SJZB
  export MAJOR_OR_MINOR=major

  curl -H X-Dataverse-key:$API_TOKEN -X POST \""$SERVER_URL/api/datasets/:persistentId/actions/:publish?persistentId=$PERSISTENT_ID&type=$MAJOR_OR_MINOR"\"

The fully expanded example above (without environment variables) looks like this:

.. code-block:: bash

  curl -H X-Dataverse-key:xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx -X POST "https://demo.dataverse.org/api/datasets/:persistentId/actions/:publish?persistentId=doi:10.5072/FK2/J8SJZB&type=major"

The quotes around the URL are required because there is more than one query parameter separated by an ampersand (``&``), which has special meaning to Unix shells such as Bash. Putting the ``&`` in quotes ensures that "type" is interpreted as one of the query parameters.

You should expect JSON output and a 200 ("OK") response in most cases. If you receive a 202 ("ACCEPTED") response, this is normal for installations that have workflows configured. Workflows are described in the :doc:`/developers/workflows` section of the Developer Guide.

.. note:: POST should be used to publish a dataset. GET is supported for backward compatibility but is deprecated and may be removed: https://github.com/IQSS/dataverse/issues/2431

Delete Dataset Draft
~~~~~~~~~~~~~~~~~~~~

Deletes the draft version of dataset ``$id``. Only the draft version can be deleted::

    DELETE http://$SERVER/api/datasets/$id/versions/:draft?key=$apiKey

Set Citation Date Field for a Dataset
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Sets the dataset field type to be used as the citation date for the given dataset (if the dataset does not include the dataset field type, the default logic is used). The name of the dataset field type should be sent in the body of the request.
To revert to the default logic, use ``:publicationDate`` as the ``$datasetFieldTypeName``.
Note that the dataset field used has to be a date field::

    PUT http://$SERVER/api/datasets/$id/citationdate?key=$apiKey --data "$datasetFieldTypeName"

Revert Citation Date Field to Default for Dataset
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Restores the default logic of the field type to be used as the citation date. Same as ``PUT`` with ``:publicationDate`` body::

    DELETE http://$SERVER/api/datasets/$id/citationdate?key=$apiKey

List Role Assignments for a Dataset
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

List all the role assignments at the given dataset::

    GET http://$SERVER/api/datasets/$id/assignments?key=$apiKey

Create a Private URL for a Dataset
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Create a Private URL (must be able to manage dataset permissions)::

    POST http://$SERVER/api/datasets/$id/privateUrl?key=$apiKey

Get the Private URL for a Dataset
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Get a Private URL from a dataset (if available)::

    GET http://$SERVER/api/datasets/$id/privateUrl?key=$apiKey

Delete the Private URL from a Dataset
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Delete a Private URL from a dataset (if it exists)::

    DELETE http://$SERVER/api/datasets/$id/privateUrl?key=$apiKey

.. _add-file-api: 

Add a File to a Dataset
~~~~~~~~~~~~~~~~~~~~~~~

When adding a file to a dataset, you can optionally specify the following:

- A description of the file.
- The "File Path" of the file, indicating which folder the file should be uploaded to within the dataset.
- Whether or not the file is restricted.

In the curl example below, all of the above are specified but they are optional.

.. note:: See :ref:`curl-examples-and-environment-variables` if you are unfamiliar with the use of ``export`` below.

.. code-block:: bash

  export API_TOKEN=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
  export FILENAME='data.tsv'
  export SERVER_URL=https://demo.dataverse.org
  export PERSISTENT_ID=doi:10.5072/FK2/J8SJZB

  curl -H X-Dataverse-key:$API_TOKEN -X POST -F "file=@$FILENAME" -F 'jsonData={"description":"My description.","directoryLabel":"data/subdir1","categories":["Data"], "restrict":"false"}' "$SERVER_URL/api/datasets/:persistentId/add?persistentId=$PERSISTENT_ID"

The fully expanded example above (without environment variables) looks like this:

.. code-block:: bash

  curl -H X-Dataverse-key:xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx -X POST -F file=@data.tsv -F jsonData={"description":"My description.","directoryLabel":"data/subdir1","categories":["Data"], "restrict":"false"} https://demo.dataverse.org/api/datasets/:persistentId/add?persistentId=doi:10.5072/FK2/J8SJZB

You should expect a 201 ("CREATED") response and JSON indicating the database id that has been assigned to your newly uploaded file.

Please note that it's possible to "trick" Dataverse into giving a file a content type (MIME type) of your choosing. For example, you can make a text file be treated like a video file with ``-F 'file=@README.txt;type=video/mpeg4'``, for example. If Dataverse does not properly detect a file type, specifying the content type via API like this a potential workaround.

The curl syntax above to upload a file is tricky and a Python version is provided below. (Please note that it depends on libraries such as "requests" that you may need to install but this task is out of scope for this guide.) Here are some parameters you can set in the script:

* ``dataverse_server`` - e.g. https://demo.dataverse.org
* ``api_key`` - See the top of this document for a description
* ``persistentId`` - Example: ``doi:10.5072/FK2/6XACVA``
* ``dataset_id`` - Database id of the dataset

In practice, you only need one the ``dataset_id`` or the ``persistentId``. The example below shows both uses.

.. code-block:: python

    from datetime import datetime
    import json
    import requests  # http://docs.python-requests.org/en/master/

    # --------------------------------------------------
    # Update the 4 params below to run this code
    # --------------------------------------------------
    dataverse_server = 'https://your dataverse server' # no trailing slash
    api_key = 'api key'
    dataset_id = 1  # database id of the dataset
    persistentId = 'doi:10.5072/FK2/6XACVA' # doi or hdl of the dataset

    # --------------------------------------------------
    # Prepare "file"
    # --------------------------------------------------
    file_content = 'content: %s' % datetime.now()
    files = {'file': ('sample_file.txt', file_content)}

    # --------------------------------------------------
    # Using a "jsonData" parameter, add optional description + file tags
    # --------------------------------------------------
    params = dict(description='Blue skies!',
                categories=['Lily', 'Rosemary', 'Jack of Hearts'])

    params_as_json_string = json.dumps(params)

    payload = dict(jsonData=params_as_json_string)

    # --------------------------------------------------
    # Add file using the Dataset's id
    # --------------------------------------------------
    url_dataset_id = '%s/api/datasets/%s/add?key=%s' % (dataverse_server, dataset_id, api_key)

    # -------------------
    # Make the request
    # -------------------
    print '-' * 40
    print 'making request: %s' % url_dataset_id
    r = requests.post(url_dataset_id, data=payload, files=files)

    # -------------------
    # Print the response
    # -------------------
    print '-' * 40
    print r.json()
    print r.status_code

    # --------------------------------------------------
    # Add file using the Dataset's persistentId (e.g. doi, hdl, etc)
    # --------------------------------------------------
    url_persistent_id = '%s/api/datasets/:persistentId/add?persistentId=%s&key=%s' % (dataverse_server, persistentId, api_key)

    # -------------------
    # Update the file content to avoid a duplicate file error
    # -------------------
    file_content = 'content2: %s' % datetime.now()
    files = {'file': ('sample_file2.txt', file_content)}


    # -------------------
    # Make the request
    # -------------------
    print '-' * 40
    print 'making request: %s' % url_persistent_id
    r = requests.post(url_persistent_id, data=payload, files=files)

    # -------------------
    # Print the response
    # -------------------
    print '-' * 40
    print r.json()
    print r.status_code

Submit a Dataset for Review
~~~~~~~~~~~~~~~~~~~~~~~~~~~

When dataset authors do not have permission to publish directly, they can click the "Submit for Review" button in the web interface (see :doc:`/user/dataset-management`), or perform the equivalent operation via API::

    curl -H "X-Dataverse-key: $API_TOKEN" -X POST "$SERVER_URL/api/datasets/:persistentId/submitForReview?persistentId=$DOI_OR_HANDLE_OF_DATASET"

The people who need to review the dataset (often curators or journal editors) can check their notifications periodically via API to see if any new datasets have been submitted for review and need their attention. See the :ref:`Notifications` section for details. Alternatively, these curators can simply check their email or notifications to know when datasets have been submitted (or resubmitted) for review.

Return a Dataset to Author
~~~~~~~~~~~~~~~~~~~~~~~~~~

After the curators or journal editors have reviewed a dataset that has been submitted for review (see "Submit for Review", above) they can either choose to publish the dataset (see the ``:publish`` "action" above) or return the dataset to its authors. In the web interface there is a "Return to Author" button (see :doc:`/user/dataset-management`), but the interface does not provide a way to explain **why** the dataset is being returned. There is a way to do this outside of this interface, however. Instead of clicking the "Return to Author" button in the UI, a curator can write a "reason for return" into the database via API.

Here's how curators can send a "reason for return" to the dataset authors. First, the curator creates a JSON file that contains the reason for return:

.. literalinclude:: ../_static/api/reason-for-return.json

In the example below, the curator has saved the JSON file as :download:`reason-for-return.json <../_static/api/reason-for-return.json>` in their current working directory. Then, the curator sends this JSON file to the ``returnToAuthor`` API endpoint like this::

    curl -H "Content-type:application/json" -d @reason-for-return.json -H "X-Dataverse-key: $API_TOKEN" -X POST "$SERVER_URL/api/datasets/:persistentId/returnToAuthor?persistentId=$DOI_OR_HANDLE_OF_DATASET"

The review process can sometimes resemble a tennis match, with the authors submitting and resubmitting the dataset over and over until the curators are satisfied. Each time the curators send a "reason for return" via API, that reason is persisted into the database, stored at the dataset version level.

Link a Dataset
~~~~~~~~~~~~~~

Creates a link between a dataset and a dataverse (see the Linked Dataverses + Linked Datasets section of the :doc:`/user/dataverse-management` guide for more information). ::

    curl -H "X-Dataverse-key: $API_TOKEN" -X PUT http://$SERVER/api/datasets/$linked-dataset-id/link/$linking-dataverse-alias

Dataset Locks
~~~~~~~~~~~~~

To check if a dataset is locked:: 

    curl "$SERVER_URL/api/datasets/{database_id}/locks

Optionally, you can check if there's a lock of a specific type on the dataset:: 

    curl "$SERVER_URL/api/datasets/{database_id}/locks?type={lock_type}

Currently implemented lock types are ``Ingest, Workflow, InReview, DcmUpload, pidRegister, and EditInProgress``. 

The API will output the list of locks, for example:: 

    {"status":"OK","data":
	[
		{
		 "lockType":"Ingest",
		 "date":"Fri Aug 17 15:05:51 EDT 2018",
		 "user":"dataverseAdmin"
		},
		{
		 "lockType":"Workflow",
		 "date":"Fri Aug 17 15:02:00 EDT 2018",
		 "user":"dataverseAdmin"
		}
	]
    }

If the dataset is not locked (or if there is no lock of the requested type), the API will return an empty list. 

The following API end point will lock a Dataset with a lock of specified type::

    POST /api/datasets/{database_id}/lock/{lock_type}

For example::

    curl -X POST "$SERVER_URL/api/datasets/1234/lock/Ingest?key=$ADMIN_API_TOKEN"
    or 
    curl -X POST -H "X-Dataverse-key: $ADMIN_API_TOKEN" "$SERVER_URL/api/datasets/:persistentId/lock/Ingest?persistentId=$DOI_OR_HANDLE_OF_DATASET"

Use the following API to unlock the dataset, by deleting all the locks currently on the dataset::

    DELETE /api/datasets/{database_id}/locks

Or, to delete a lock of the type specified only::

    DELETE /api/datasets/{database_id}/locks?type={lock_type}

For example::

    curl -X DELETE -H "X-Dataverse-key: $ADMIN_API_TOKEN" "$SERVER_URL/api/datasets/1234/locks?type=pidRegister"

If the dataset is not locked (or if there is no lock of the specified type), the API will exit with a warning message. 

(Note that the API calls above all support both the database id and persistent identifier notation for referencing the dataset)

.. _dataset-metrics-api:

Dataset Metrics
~~~~~~~~~~~~~~~

Please note that these dataset level metrics are only available if support for Make Data Count has been enabled in your installation of Dataverse. See the :ref:`Dataset Metrics <dataset-metrics-user>` in the :doc:`/user/dataset-management` section of the User Guide and the :doc:`/admin/make-data-count` section of the Admin Guide for details.

.. note:: See :ref:`curl-examples-and-environment-variables` if you are unfamiliar with the use of ``export`` below.

``export DV_BASE_URL=https://demo.dataverse.org``

To confirm that the environment variable was set properly, you can use ``echo`` like this:

``echo $DV_BASE_URL``

Please note that for each of these endpoints except the "citations" endpoint, you can optionally pass the query parameter "country" with a two letter code (e.g. "country=us") and you can specify a particular month by adding it in yyyy-mm format after the requested metric (e.g. "viewsTotal/2019-02").

Retrieving Total Views for a Dataset
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Please note that "viewsTotal" is a combination of "viewsTotalRegular" and "viewsTotalMachine" which can be requested separately.

``curl "$DV_BASE_URL/api/datasets/:persistentId/makeDataCount/viewsTotal?persistentId=$DOI"``

Retrieving Unique Views for a Dataset
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Please note that "viewsUnique" is a combination of "viewsUniqueRegular" and "viewsUniqueMachine" which can be requested separately.

``curl "$DV_BASE_URL/api/datasets/:persistentId/makeDataCount/viewsUnique?persistentId=$DOI"``

Retrieving Total Downloads for a Dataset
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Please note that "downloadsTotal" is a combination of "downloadsTotalRegular" and "downloadsTotalMachine" which can be requested separately.

``curl "$DV_BASE_URL/api/datasets/:persistentId/makeDataCount/downloadsTotal?persistentId=$DOI"``

Retrieving Unique Downloads for a Dataset
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Please note that "downloadsUnique" is a combination of "downloadsUniqueRegular" and "downloadsUniqueMachine" which can be requested separately.

``curl "$DV_BASE_URL/api/datasets/:persistentId/makeDataCount/downloadsUnique?persistentId=$DOI"``

Retrieving Citations for a Dataset
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

``curl "$DV_BASE_URL/api/datasets/:persistentId/makeDataCount/citations?persistentId=$DOI"``

Delete Unpublished Dataset
~~~~~~~~~~~~~~~~~~~~~~~~~~

Delete the dataset whose id is passed:

``curl -H "X-Dataverse-key:$API_TOKEN" -X DELETE http://$SERVER/api/datasets/$id``

Delete Published Dataset
~~~~~~~~~~~~~~~~~~~~~~~~

Normally published datasets should not be deleted, but there exists a "destroy" API endpoint for superusers which will act on a dataset given a persistent ID or dataset database ID:

``curl -H "X-Dataverse-key:$API_TOKEN" -X DELETE http://$SERVER/api/datasets/:persistentId/destroy/?persistentId=doi:10.5072/FK2/AAA000``
  
``curl -H "X-Dataverse-key:$API_TOKEN" -X DELETE http://$SERVER/api/datasets/999/destroy``
  
Calling the destroy endpoint is permanent and irreversible. It will remove the dataset and its datafiles, then re-index the parent dataverse in Solr. This endpoint requires the API token of a superuser.

Files
-----

Adding Files
~~~~~~~~~~~~

.. Note:: Files can be added via the native API but the operation is performed on the parent object, which is a dataset. Please see the Datasets_ endpoint above for more information.

Accessing (downloading) files
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. Note:: Access API has its own section in the Guide: :doc:`/api/dataaccess`

**Note** Data Access API calls can now be made using persistent identifiers (in addition to database ids). This is done by passing the constant ``:persistentId`` where the numeric id of the file is expected, and then passing the actual persistent id as a query parameter with the name ``persistentId``.

  Example: Getting the file whose DOI is *10.5072/FK2/J8SJZB* ::

    GET http://$SERVER/api/access/datafile/:persistentId/?persistentId=doi:10.5072/FK2/J8SJZB


Restrict Files
~~~~~~~~~~~~~~

Restrict or unrestrict an existing file where ``id`` is the database id of the file or ``pid`` is the persistent id (DOI or Handle) of the file to restrict. Note that some Dataverse installations do not allow the ability to restrict files.

A curl example using an ``id``::

    curl -H "X-Dataverse-key:$API_TOKEN" -X PUT -d true http://$SERVER/api/files/{id}/restrict

A curl example using a ``pid``::

    curl -H "X-Dataverse-key:$API_TOKEN" -X PUT -d true http://$SERVER/api/files/:persistentId/restrict?persistentId={pid}
    
Uningest a File
~~~~~~~~~~~~~~~

Reverse the tabular data ingest process performed on a file where ``{id}`` is the database id of the file to process. Note that this requires "superuser" credentials::

    POST http://$SERVER/api/files/{id}/uningest?key={apiKey}

Reingest a File
~~~~~~~~~~~~~~~

Attempt to ingest an existing datafile as tabular data. This API can be used on a file that was not ingested as tabular back when it was uploaded. For example, a Stata v.14 file that was uploaded before ingest support for Stata 14 was added (in Dataverse v.4.9). It can also be used on a file that failed to ingest due to a bug in the ingest plugin that has since been fixed (hence the name "reingest").

Note that this requires "superuser" credentials:: 

    POST http://$SERVER/api/files/{id}/reingest?key={apiKey}

(``{id}`` is the database id of the file to process)

Note: at present, the API cannot be used on a file that's already successfully ingested as tabular.

.. _redetect-file-type:

Redetect File Type
~~~~~~~~~~~~~~~~~~

Dataverse uses a variety of methods for determining file types (MIME types or content types) and these methods (listed below) are updated periodically. If you have files that have an unknown file type, you can have Dataverse attempt to redetect the file type.

When using the curl command below, you can pass ``dryRun=true`` if you don't want any changes to be saved to the database. Change this to ``dryRun=false`` (or omit it) to save the change. In the example below, the file is identified by database id "42".

``export FILE_ID=42``

``curl -H "X-Dataverse-key:$API_TOKEN" -X POST $SERVER_URL/api/files/$FILE_ID/redetect?dryRun=true``

Currently the following methods are used to detect file types:

- The file type detected by the browser (or sent via API).
- JHOVE: http://jhove.openpreservation.org
- As a last resort the file extension (e.g. ".ipybn") is used, defined in a file called ``MimeTypeDetectionByFileExtension.properties``.

Replacing Files
~~~~~~~~~~~~~~~

Replace an existing file where ``id`` is the database id of the file to replace or ``pid`` is the persistent id (DOI or Handle) of the file. Requires the ``file`` to be passed as well as a ``jsonString`` expressing the new metadata.  Note that metadata such as description, directoryLabel (File Path) and tags are not carried over from the file being replaced::

    POST -F 'file=@file.extension' -F 'jsonData={json}' http://$SERVER/api/files/{id}/metadata?key={apiKey}

Example::

  curl -H "X-Dataverse-key:$API_TOKEN" -X POST -F 'file=@data.tsv' \
  -F 'jsonData={"description":"My description.","categories":["Data"],"forceReplace":false}'\
  "https://demo.dataverse.org/api/files/$FILE_ID/replace"

Getting File Metadata
~~~~~~~~~~~~~~~~~~~~~

Provides a json representation of the file metadata for an existing file where ``id`` is the database id of the file to replace or ``pid`` is the persistent id (DOI or Handle) of the file::

    GET http://$SERVER/api/files/{id}/metadata

The current draft can also be viewed if you have permissions and pass your ``apiKey``::

    GET http://$SERVER/api/files/{id}/metadata/draft?key={apiKey}

Note: The ``id`` returned in the json response is the id of the file metadata version.

Updating File Metadata
~~~~~~~~~~~~~~~~~~~~~~

Updates the file metadata for an existing file where ``id`` is the database id of the file to replace or ``pid`` is the persistent id (DOI or Handle) of the file. Requires a ``jsonString`` expressing the new metadata. No metadata from the previous version of this file will be persisted, so if you want to update a specific field first get the json with the above command and alter the fields you want::

    POST -F 'jsonData={json}' http://$SERVER/api/files/{id}/metadata?key={apiKey}

Example::

    curl -H "X-Dataverse-key:{apiKey}" -X POST -F 'jsonData={"description":"My description bbb.","provFreeform":"Test prov freeform","categories":["Data"],"restrict":false}' 'http://localhost:8080/api/files/264/metadata'

Also note that dataFileTags are not versioned and changes to these will update the published version of the file.

.. _EditingVariableMetadata:

Editing Variable Level Metadata
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Updates variable level metadata using ddi xml ``$file``, where ``$id`` is file id::

    PUT https://$SERVER/api/edit/$id --upload-file $file

Example: ``curl -H "X-Dataverse-key:$API_TOKEN" -X PUT http://localhost:8080/api/edit/95 --upload-file dct.xml``

You can download :download:`dct.xml <../../../../src/test/resources/xml/dct.xml>` from the example above to see what the XML looks like.

Provenance
~~~~~~~~~~
Get Provenance JSON for an uploaded file::

    GET http://$SERVER/api/files/{id}/prov-json?key=$apiKey

Get Provenance Description for an uploaded file::

    GET http://$SERVER/api/files/{id}/prov-freeform?key=$apiKey

Create/Update Provenance JSON and provide related entity name for an uploaded file::

    POST http://$SERVER/api/files/{id}/prov-json?key=$apiKey&entityName=$entity -H "Content-type:application/json" --upload-file $filePath

Create/Update Provenance Description for an uploaded file. Requires a JSON file with the description connected to a key named "text"::

    POST http://$SERVER/api/files/{id}/prov-freeform?key=$apiKey -H "Content-type:application/json" --upload-file $filePath

Delete Provenance JSON for an uploaded file::

    DELETE http://$SERVER/api/files/{id}/prov-json?key=$apiKey

Datafile Integrity
~~~~~~~~~~~~~~~~~~

Starting the release 4.10 the size of the saved original file (for an ingested tabular datafile) is stored in the database. The following API will retrieve and permanently store the sizes for any already existing saved originals::

	    GET http://$SERVER/api/admin/datafiles/integrity/fixmissingoriginalsizes{?limit=N}

Note the optional "limit" parameter. Without it, the API will attempt to populate the sizes for all the saved originals that don't have them in the database yet. Otherwise it will do so for the first N such datafiles. 

Builtin Users
-------------

Builtin users are known as "Username/Email and Password" users in the :doc:`/user/account` of the User Guide. Dataverse stores a password (encrypted, of course) for these users, which differs from "remote" users such as Shibboleth or OAuth users where the password is stored elsewhere. See also "Auth Modes: Local vs. Remote vs. Both" in the :doc:`/installation/config` section of the Installation Guide. It's a valid configuration of Dataverse to not use builtin users at all.

Create a Builtin User
~~~~~~~~~~~~~~~~~~~~~

For security reasons, builtin users cannot be created via API unless the team who runs the Dataverse installation has populated a database setting called ``BuiltinUsers.KEY``, which is described under "Securing Your Installation" and "Database Settings" in the :doc:`/installation/config` section of the Installation Guide. You will need to know the value of ``BuiltinUsers.KEY`` before you can proceed.

To create a builtin user via API, you must first construct a JSON document.  You can download :download:`user-add.json <../_static/api/user-add.json>` or copy the text below as a starting point and edit as necessary.

.. literalinclude:: ../_static/api/user-add.json

Place this ``user-add.json`` file in your current directory and run the following curl command, substituting variables as necessary. Note that both the password of the new user and the value of ``BuiltinUsers.KEY`` are passed as query parameters::

  curl -d @user-add.json -H "Content-type:application/json" "$SERVER_URL/api/builtin-users?password=$NEWUSER_PASSWORD&key=$BUILTIN_USERS_KEY"

Roles
-----

Create a New Role in a Dataverse
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Creates a new role in dataverse object whose Id is ``dataverseIdtf`` (that's an id/alias)::

  POST http://$SERVER/api/roles?dvo=$dataverseIdtf&key=$apiKey

Show Role
~~~~~~~~~

Shows the role with ``id``::

  GET http://$SERVER/api/roles/$id

Delete Role
~~~~~~~~~~~

Deletes the role with ``id``::

  DELETE http://$SERVER/api/roles/$id

Explicit Groups
---------------

Create New Explicit Group
~~~~~~~~~~~~~~~~~~~~~~~~~

Explicit groups list their members explicitly. These groups are defined in dataverses, which is why their API endpoint is under ``api/dataverses/$id/``, where ``$id`` is the id of the dataverse.

Create a new explicit group under dataverse ``$id``::

  POST http://$server/api/dataverses/$id/groups

Data being POSTed is json-formatted description of the group::

  {
   "description":"Describe the group here",
   "displayName":"Close Collaborators",
   "aliasInOwner":"ccs"
  }

List Explicit Groups in a Dataverse
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

List explicit groups under dataverse ``$id``::

  GET http://$server/api/dataverses/$id/groups

Show Single Group in a Dataverse
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Show group ``$groupAlias`` under dataverse ``$dv``::

  GET http://$server/api/dataverses/$dv/groups/$groupAlias

Update Group in a Dataverse
~~~~~~~~~~~~~~~~~~~~~~~~~~~

Update group ``$groupAlias`` under dataverse ``$dv``. The request body is the same as the create group one, except that the group alias cannot be changed. Thus, the field ``aliasInOwner`` is ignored. ::

  PUT http://$server/api/dataverses/$dv/groups/$groupAlias

Delete Group from a Dataverse
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Delete group ``$groupAlias`` under dataverse ``$dv``::

  DELETE http://$server/api/dataverses/$dv/groups/$groupAlias

Add Multiple Role Assignees to an Explicit Group
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Bulk add role assignees to an explicit group. The request body is a JSON array of role assignee identifiers, such as ``@admin``, ``&ip/localhosts`` or ``:authenticated-users``::

  POST http://$server/api/dataverses/$dv/groups/$groupAlias/roleAssignees

Add a Role Assignee to an Explicit Group
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Add a single role assignee to a group. Request body is ignored::

  PUT http://$server/api/dataverses/$dv/groups/$groupAlias/roleAssignees/$roleAssigneeIdentifier

Remove a Role Assignee from an Explicit Group
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Remove a single role assignee from an explicit group::

  DELETE http://$server/api/dataverses/$dv/groups/$groupAlias/roleAssignees/$roleAssigneeIdentifier

Shibboleth Groups
-----------------

Management of Shibboleth groups via API is documented in the :doc:`/installation/shibboleth` section of the Installation Guide.

Info
----

Show Dataverse Version and Build Number
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

|CORS| Get the Dataverse version. The response contains the version and build numbers:

.. note:: See :ref:`curl-examples-and-environment-variables` if you are unfamiliar with the use of export below.

.. code-block:: bash

  export SERVER_URL=https://demo.dataverse.org

  curl $SERVER_URL/api/info/version

The fully expanded example above (without environment variables) looks like this:

.. code-block:: bash

  curl https://demo.dataverse.org/api/info/version

Show Dataverse Server Name
~~~~~~~~~~~~~~~~~~~~~~~~~~

Get the server name. This is useful when a Dataverse system is composed of multiple Java EE servers behind a load balancer:

.. note:: See :ref:`curl-examples-and-environment-variables` if you are unfamiliar with the use of export below.

.. code-block:: bash

  export SERVER_URL=https://demo.dataverse.org

  curl $SERVER_URL/api/info/server

The fully expanded example above (without environment variables) looks like this:

.. code-block:: bash

  curl https://demo.dataverse.org/api/info/server

Show Custom Popup Text for Publishing Datasets
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

For now, only the value for the ``:DatasetPublishPopupCustomText`` setting from the :doc:`/installation/config` section of the Installation Guide is exposed:

.. note:: See :ref:`curl-examples-and-environment-variables` if you are unfamiliar with the use of export below.

.. code-block:: bash

  export SERVER_URL=https://demo.dataverse.org

  curl $SERVER_URL/api/info/settings/:DatasetPublishPopupCustomText

The fully expanded example above (without environment variables) looks like this:

.. code-block:: bash

  curl https://demo.dataverse.org/api/info/settings/:DatasetPublishPopupCustomText

Get API Terms of Use URL
~~~~~~~~~~~~~~~~~~~~~~~~

Get API Terms of Use. The response contains the text value inserted as API Terms of use which uses the database setting  ``:ApiTermsOfUse``:

.. note:: See :ref:`curl-examples-and-environment-variables` if you are unfamiliar with the use of export below.

.. code-block:: bash

  export SERVER_URL=https://demo.dataverse.org

  curl $SERVER_URL/api/info/apiTermsOfUse

The fully expanded example above (without environment variables) looks like this:

.. code-block:: bash

  curl https://demo.dataverse.org/api/info/apiTermsOfUse

Metadata Blocks
---------------

Show Info About All Metadata Blocks
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

|CORS| Lists brief info about all metadata blocks registered in the system::

  GET http://$SERVER/api/metadatablocks

Show Info About Single Metadata Block
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

|CORS| Return data about the block whose ``identifier`` is passed. ``identifier`` can either be the block's id, or its name::

  GET http://$SERVER/api/metadatablocks/$identifier

.. _Notifications:

Notifications
-------------

Get All Notifications by User
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Each user can get a dump of their notifications by passing in their API token::

    curl -H "X-Dataverse-key:$API_TOKEN" $SERVER_URL/api/notifications/all

Admin
-----

This is the administrative part of the API. For security reasons, it is absolutely essential that you block it before allowing public access to a Dataverse installation. Blocking can be done using settings. See the ``post-install-api-block.sh`` script in the ``scripts/api`` folder for details. See also "Blocking API Endpoints" under "Securing Your Installation" in the :doc:`/installation/config` section of the Installation Guide.

List All Database Settings
~~~~~~~~~~~~~~~~~~~~~~~~~~

List all settings::

  GET http://$SERVER/api/admin/settings

Configure Database Setting
~~~~~~~~~~~~~~~~~~~~~~~~~~

Sets setting ``name`` to the body of the request::

  PUT http://$SERVER/api/admin/settings/$name

Get Single Database Setting
~~~~~~~~~~~~~~~~~~~~~~~~~~~

Get the setting under ``name``::

  GET http://$SERVER/api/admin/settings/$name

Delete Database Setting
~~~~~~~~~~~~~~~~~~~~~~~

Delete the setting under ``name``::

  DELETE http://$SERVER/api/admin/settings/$name

List Authentication Provider Factories
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

List the authentication provider factories. The alias field of these is used while configuring the providers themselves. ::

  GET http://$SERVER/api/admin/authenticationProviderFactories

List Authentication Providers
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

List all the authentication providers in the system (both enabled and disabled)::

  GET http://$SERVER/api/admin/authenticationProviders

.. _native-api-add-auth-provider:

Add Authentication Provider
~~~~~~~~~~~~~~~~~~~~~~~~~~~

Add new authentication provider. The POST data is in JSON format, similar to the JSON retrieved from this command's ``GET`` counterpart. ::

  POST http://$SERVER/api/admin/authenticationProviders

Show Authentication Provider
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Show data about an authentication provider::

  GET http://$SERVER/api/admin/authenticationProviders/$id


.. _api-toggle-auth-provider:

Enable or Disable an Authentication Provider
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Enable or disable an authentication provider (denoted by ``id``)::

  PUT http://$SERVER/api/admin/authenticationProviders/$id/enabled

.. note:: The former endpoint, ending with ``:enabled`` (that is, with a colon), is still supported, but deprecated.

Check If an Authentication Provider is Enabled
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Check whether an authentication proider is enabled::

  GET http://$SERVER/api/admin/authenticationProviders/$id/enabled

The body of the request should be either ``true`` or ``false``. Content type has to be ``application/json``, like so::

  curl -H "Content-type: application/json"  -X POST -d"false" http://localhost:8080/api/admin/authenticationProviders/echo-dignified/:enabled

Delete an Authentication Provider
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Deletes an authentication provider from the system. The command succeeds even if there is no such provider, as the postcondition holds: there is no provider by that id after the command returns. ::

  DELETE http://$SERVER/api/admin/authenticationProviders/$id/

List Global Roles
~~~~~~~~~~~~~~~~~~

List all global roles in the system. ::

    GET http://$SERVER/api/admin/roles

Create Global Role
~~~~~~~~~~~~~~~~~~

Creates a global role in the Dataverse installation. The data POSTed are assumed to be a role JSON. ::

    POST http://$SERVER/api/admin/roles

List Users
~~~~~~~~~~

List users with the options to search and "page" through results. Only accessible to superusers. Optional parameters:

* ``searchTerm`` A string that matches the beginning of a user identifier, first name, last name or email address.
* ``itemsPerPage`` The number of detailed results to return.  The default is 25.  This number has no limit. e.g. You could set it to 1000 to return 1,000 results
* ``selectedPage`` The page of results to return.  The default is 1.

::

    GET http://$SERVER/api/admin/list-users


Sample output appears below.

* When multiple pages of results exist, the ``selectedPage`` parameters may be specified.
* Note, the resulting ``pagination`` section includes ``pageCount``, ``previousPageNumber``, ``nextPageNumber``, and other variables that may be used to re-create the UI.

.. code-block:: text

    {
        "status":"OK",
        "data":{
            "userCount":27,
            "selectedPage":1,
            "pagination":{
                "isNecessary":true,
                "numResults":27,
                "numResultsString":"27",
                "docsPerPage":25,
                "selectedPageNumber":1,
                "pageCount":2,
                "hasPreviousPageNumber":false,
                "previousPageNumber":1,
                "hasNextPageNumber":true,
                "nextPageNumber":2,
                "startResultNumber":1,
                "endResultNumber":25,
                "startResultNumberString":"1",
                "endResultNumberString":"25",
                "remainingResults":2,
                "numberNextResults":2,
                "pageNumberList":[
                    1,
                    2
                ]
            },
            "bundleStrings":{
                "userId":"ID",
                "userIdentifier":"Username",
                "lastName":"Last Name ",
                "firstName":"First Name ",
                "email":"Email",
                "affiliation":"Affiliation",
                "position":"Position",
                "isSuperuser":"Superuser",
                "authenticationProvider":"Authentication",
                "roles":"Roles",
                "createdTime":"Created Time",
                "lastLoginTime":"Last Login Time",
                "lastApiUseTime":"Last API Use Time"
            },
            "users":[
                {
                    "id":8,
                    "userIdentifier":"created1",
                    "lastName":"created1",
                    "firstName":"created1",
                    "email":"created1@g.com",
                    "affiliation":"hello",
                    "isSuperuser":false,
                    "authenticationProvider":"BuiltinAuthenticationProvider",
                    "roles":"Curator",
                    "createdTime":"2017-06-28 10:36:29.444"
                },
                {
                    "id":9,
                    "userIdentifier":"created8",
                    "lastName":"created8",
                    "firstName":"created8",
                    "email":"created8@g.com",
                    "isSuperuser":false,
                    "authenticationProvider":"BuiltinAuthenticationProvider",
                    "roles":"Curator",
                    "createdTime":"2000-01-01 00:00:00.0"
                },
                {
                    "id":1,
                    "userIdentifier":"dataverseAdmin",
                    "lastName":"Admin",
                    "firstName":"Dataverse",
                    "email":"dataverse@mailinator2.com",
                    "affiliation":"Dataverse.org",
                    "position":"Admin",
                    "isSuperuser":true,
                    "authenticationProvider":"BuiltinAuthenticationProvider",
                    "roles":"Admin, Contributor",
                    "createdTime":"2000-01-01 00:00:00.0",
                    "lastLoginTime":"2017-07-03 12:22:35.926",
                    "lastApiUseTime":"2017-07-03 12:55:57.186"
                }

                // ... 22 more user documents ...
            ]
        }
    }

.. note:: "List all users" ``GET http://$SERVER/api/admin/authenticatedUsers`` is deprecated, but supported.

List Single User
~~~~~~~~~~~~~~~~

List user whose ``identifier`` (without the ``@`` sign) is passed::

    GET http://$SERVER/api/admin/authenticatedUsers/$identifier

Sample output using "dataverseAdmin" as the ``identifier``::

    {
      "authenticationProviderId": "builtin",
      "persistentUserId": "dataverseAdmin",
      "position": "Admin",
      "id": 1,
      "identifier": "@dataverseAdmin",
      "displayName": "Dataverse Admin",
      "firstName": "Dataverse",
      "lastName": "Admin",
      "email": "dataverse@mailinator.com",
      "superuser": true,
      "affiliation": "Dataverse.org"
    }

Create an authenticateUser::

    POST http://$SERVER/api/admin/authenticatedUsers

POSTed JSON example::

    {
      "authenticationProviderId": "orcid",
      "persistentUserId": "0000-0002-3283-0661",
      "identifier": "@pete",
      "firstName": "Pete K.",
      "lastName": "Dataversky",
      "email": "pete@mailinator.com"
    }

.. _merge-accounts-label:

Merge User Accounts
~~~~~~~~~~~~~~~~~~~

If a user has created multiple accounts and has been performed actions under both accounts that need to be preserved, these accounts can be combined.  One account can be merged into another account and all data associated with both accounts will be combined in the surviving account. Only accessible to superusers.::

    POST https://$SERVER/api/users/$toMergeIdentifier/mergeIntoUser/$continuingIdentifier

Example: ``curl -H "X-Dataverse-key: $API_TOKEN" -X POST http://demo.dataverse.org/api/users/jsmith2/mergeIntoUser/jsmith``

This action moves account data from jsmith2 into the account jsmith and deletes the account of jsmith2.

.. _change-identifier-label:

Change User Identifier
~~~~~~~~~~~~~~~~~~~~~~

Changes identifier for user in ``AuthenticatedUser``, ``BuiltinUser``, ``AuthenticatedUserLookup`` & ``RoleAssignment``. Allows them to log in with the new identifier. Only accessible to superusers.::

    PUT http://$SERVER/api/users/$oldIdentifier/changeIdentifier/$newIdentifier

Example: ``curl -H "X-Dataverse-key: $API_TOKEN" -X POST  https://demo.dataverse.org/api/users/johnsmith/changeIdentifier/jsmith``

This action changes the identifier of user johnsmith to jsmith.

Make User a SuperUser
~~~~~~~~~~~~~~~~~~~~~

Toggles superuser mode on the ``AuthenticatedUser`` whose ``identifier`` (without the ``@`` sign) is passed. ::

    POST http://$SERVER/api/admin/superuser/$identifier

List Role Assignments of a Role Assignee
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

List all role assignments of a role assignee (i.e. a user or a group)::

    GET http://$SERVER/api/admin/assignments/assignees/$identifier

Note that ``identifier`` can contain slashes (e.g. ``&ip/localhost-users``).

List Permissions a User Has on a Dataverse or Dataset
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

List permissions a user (based on API Token used) has on a dataverse or dataset::

    GET http://$SERVER/api/admin/permissions/$identifier

The ``$identifier`` can be a dataverse alias or database id or a dataset persistent ID or database id.

Show Role Assignee
~~~~~~~~~~~~~~~~~~

List a role assignee (i.e. a user or a group)::

    GET http://$SERVER/api/admin/assignee/$identifier

The ``$identifier`` should start with an ``@`` if it's a user. Groups start with ``&``. "Built in" users and groups start with ``:``. Private URL users start with ``#``.

Saved Search
~~~~~~~~~~~~

The Saved Search, Linked Dataverses, and Linked Datasets features shipped with Dataverse 4.0, but as a "`superuser-only <https://github.com/IQSS/dataverse/issues/90#issuecomment-86094663>`_" because they are **experimental** (see `#1364 <https://github.com/IQSS/dataverse/issues/1364>`_, `#1813 <https://github.com/IQSS/dataverse/issues/1813>`_, `#1840 <https://github.com/IQSS/dataverse/issues/1840>`_, `#1890 <https://github.com/IQSS/dataverse/issues/1890>`_, `#1939 <https://github.com/IQSS/dataverse/issues/1939>`_, `#2167 <https://github.com/IQSS/dataverse/issues/2167>`_, `#2186 <https://github.com/IQSS/dataverse/issues/2186>`_, `#2053 <https://github.com/IQSS/dataverse/issues/2053>`_, and `#2543 <https://github.com/IQSS/dataverse/issues/2543>`_). The following API endpoints were added to help people with access to the "admin" API make use of these features in their current form. Of particular interest should be the "makelinks" endpoint because it needs to be called periodically (via cron or similar) to find new dataverses and datasets that match the saved search and then link the search results to the dataverse in which the saved search is defined (`#2531 <https://github.com/IQSS/dataverse/issues/2531>`_ shows an example). There is a known issue (`#1364 <https://github.com/IQSS/dataverse/issues/1364>`_) that once a link to a dataverse or dataset is created, it cannot be removed (apart from database manipulation and reindexing) which is why a ``DELETE`` endpoint for saved searches is neither documented nor functional. The Linked Dataverses feature is `powered by Saved Search <https://github.com/IQSS/dataverse/issues/1852>`_ and therefore requires that the "makelinks" endpoint be executed on a periodic basis as well.

List all saved searches. ::

  GET http://$SERVER/api/admin/savedsearches/list

List a saved search by database id. ::

  GET http://$SERVER/api/admin/savedsearches/$id

Execute a saved search by database id and make links to dataverses and datasets that are found. The JSON response indicates which dataverses and datasets were newly linked versus already linked. The ``debug=true`` query parameter adds to the JSON response extra information about the saved search being executed (which you could also get by listing the saved search). ::

  PUT http://$SERVER/api/admin/savedsearches/makelinks/$id?debug=true

Execute all saved searches and make links to dataverses and datasets that are found. ``debug`` works as described above.  ::

  PUT http://$SERVER/api/admin/savedsearches/makelinks/all?debug=true

Dataset Integrity
~~~~~~~~~~~~~~~~~

Recalculate the UNF value of a dataset version, if it's missing, by supplying the dataset version database id::

  POST http://$SERVER/api/admin/datasets/integrity/{datasetVersionId}/fixmissingunf

.. _dataset-validation-api:

Dataset Validation
~~~~~~~~~~~~~~~~~~

Validate the dataset and its components (DatasetVersion, FileMetadatas, etc.) for constraint violations::

  curl $SERVER_URL/api/admin/validate/dataset/{datasetId}

if validation fails, will report the specific database entity and the offending value. For example::
   
   {"status":"OK","data":{"entityClassDatabaseTableRowId":"[DatasetVersion id:73]","field":"archiveNote","invalidValue":"random text, not a url"}} 

If the optional argument ``variables=true`` is specified, the API will also validate the metadata associated with any tabular data files found in the dataset specified. (For example: an invalid or empty variable name). 

Validate all the datasets in the Dataverse, report any constraint violations found::

  curl $SERVER_URL/api/admin/validate/datasets

If the optional argument ``variables=true`` is specified, the API will also validate the metadata associated with any tabular data files. (For example: an invalid or empty variable name). Note that validating all the tabular metadata may significantly increase the run time of the full validation pass. 

This API streams its output in real time, i.e. it will start producing the output immediately and will be reporting on the progress as it validates one dataset at a time. For example:: 

     {"datasets": [
     		  {"datasetId":27,"status":"valid"},
		  {"datasetId":29,"status":"valid"},
		  {"datasetId":31,"status":"valid"},
		  {"datasetId":33,"status":"valid"},
		  {"datasetId":35,"status":"valid"},
		  {"datasetId":41,"status":"invalid","entityClassDatabaseTableRowId":"[DatasetVersion id:73]","field":"archiveNote","invalidValue":"random text, not a url"}, 
		  {"datasetId":57,"status":"valid"}
		  ]
      }

Note that if you are attempting to validate a very large number of datasets in your Dataverse, this API may time out - subject to the timeout limit set in your Glassfish configuration. If this is a production Dataverse instance serving large amounts of data, you most likely have that timeout set to some high value already. But if you need to increase it, it can be done with the asadmin command. For example::
 
     asadmin set server-config.network-config.protocols.protocol.http-listener-1.http.request-timeout-seconds=3600



Workflows
~~~~~~~~~

List all available workflows in the system::

   GET http://$SERVER/api/admin/workflows

Get details of a workflow with a given id::

   GET http://$SERVER/api/admin/workflows/$id

Add a new workflow. Request body specifies the workflow properties and steps in JSON format.
Sample ``json`` files are available at ``scripts/api/data/workflows/``::

   POST http://$SERVER/api/admin/workflows

Delete a workflow with a specific id::

    DELETE http://$SERVER/api/admin/workflows/$id

.. warning:: If the workflow designated by ``$id`` is a default workflow, a 403 FORBIDDEN response will be returned, and the deletion will be canceled.

List the default workflow for each trigger type::

  GET http://$SERVER/api/admin/workflows/default/

Set the default workflow for a given trigger. This workflow is run when a dataset is published. The body of the PUT request is the id of the workflow. Trigger types are ``PrePublishDataset, PostPublishDataset``::

  PUT http://$SERVER/api/admin/workflows/default/$triggerType

Get the default workflow for ``triggerType``. Returns a JSON representation of the workflow, if present, or 404 NOT FOUND. ::

  GET http://$SERVER/api/admin/workflows/default/$triggerType

Unset the default workflow for ``triggerType``. After this call, dataset releases are done with no workflow. ::

  DELETE http://$SERVER/api/admin/workflows/default/$triggerType

Set the whitelist of IP addresses separated by a semicolon (``;``) allowed to resume workflows. Request body is a list of IP addresses allowed to send "resume workflow" messages to this Dataverse instance::

  PUT http://$SERVER/api/admin/workflows/ip-whitelist

Get the whitelist of IP addresses allowed to resume workflows::

  GET http://$SERVER/api/admin/workflows/ip-whitelist

Restore the whitelist of IP addresses allowed to resume workflows to default (localhost only)::

  DELETE http://$SERVER/api/admin/workflows/ip-whitelist

Metrics
~~~~~~~

Clear all cached metric results::

    DELETE http://$SERVER/api/admin/clearMetricsCache

Clear a specific metric cache. Currently this must match the name of the row in the table, which is named *metricName*_*metricYYYYMM* (or just *metricName* if there is no date range for the metric). For example dataversesToMonth_2018-05::

    DELETE http://$SERVER/api/admin/clearMetricsCache/$metricDbName

.. |CORS| raw:: html

      <span class="label label-success pull-right">
        CORS
      </span>

Inherit Dataverse Role Assignments
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Recursively applies the role assignments of the specified dataverse, for the roles specified by the ``:InheritParentRoleAssignments`` setting, to all dataverses contained within it:: 

  GET http://$SERVER/api/admin/dataverse/{dataverse alias}/addRoleAssignmentsToChildren
  
Note: setting ``:InheritParentRoleAssignments`` will automatically trigger inheritance of the parent dataverse's role assignments for a newly created dataverse. Hence this API call is intended as a way to update existing child dataverses or to update children after a change in role assignments has been made on a parent dataverse.


