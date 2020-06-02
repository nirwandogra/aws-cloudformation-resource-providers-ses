# AWS::ApiGateway::RestApi

An example resource schema demonstrating some basic constructs and validation rules.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::ApiGateway::RestApi",
    "Properties" : {
        "<a href="#id" title="Id">Id</a>" : <i>String</i>,
        "<a href="#apikeysourcetype" title="ApiKeySourceType">ApiKeySourceType</a>" : <i>String</i>,
        "<a href="#binarymediatypes" title="BinaryMediaTypes">BinaryMediaTypes</a>" : <i>[ String, ... ]</i>,
        "<a href="#body" title="Body">Body</a>" : <i><a href="body.md">Body</a></i>,
        "<a href="#bodys3location" title="BodyS3Location">BodyS3Location</a>" : <i><a href="bodys3location.md">BodyS3Location</a></i>,
        "<a href="#clonefrom" title="CloneFrom">CloneFrom</a>" : <i>String</i>,
        "<a href="#endpointconfiguration" title="EndpointConfiguration">EndpointConfiguration</a>" : <i><a href="endpointconfiguration.md">EndpointConfiguration</a></i>,
        "<a href="#description" title="Description">Description</a>" : <i>String</i>,
        "<a href="#failonwarnings" title="FailOnWarnings">FailOnWarnings</a>" : <i>Boolean</i>,
        "<a href="#name" title="Name">Name</a>" : <i>String</i>,
        "<a href="#minimumcompressionsize" title="MinimumCompressionSize">MinimumCompressionSize</a>" : <i>Double</i>,
        "<a href="#mode" title="Mode">Mode</a>" : <i>String</i>,
        "<a href="#policy" title="Policy">Policy</a>" : <i><a href="policy.md">Policy</a></i>,
        "<a href="#parameters" title="Parameters">Parameters</a>" : <i><a href="parameters.md">Parameters</a></i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tags.md">Tags</a>, ... ]</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::ApiGateway::RestApi
Properties:
    <a href="#id" title="Id">Id</a>: <i>String</i>
    <a href="#apikeysourcetype" title="ApiKeySourceType">ApiKeySourceType</a>: <i>String</i>
    <a href="#binarymediatypes" title="BinaryMediaTypes">BinaryMediaTypes</a>: <i>
      - String</i>
    <a href="#body" title="Body">Body</a>: <i><a href="body.md">Body</a></i>
    <a href="#bodys3location" title="BodyS3Location">BodyS3Location</a>: <i><a href="bodys3location.md">BodyS3Location</a></i>
    <a href="#clonefrom" title="CloneFrom">CloneFrom</a>: <i>String</i>
    <a href="#endpointconfiguration" title="EndpointConfiguration">EndpointConfiguration</a>: <i><a href="endpointconfiguration.md">EndpointConfiguration</a></i>
    <a href="#description" title="Description">Description</a>: <i>String</i>
    <a href="#failonwarnings" title="FailOnWarnings">FailOnWarnings</a>: <i>Boolean</i>
    <a href="#name" title="Name">Name</a>: <i>String</i>
    <a href="#minimumcompressionsize" title="MinimumCompressionSize">MinimumCompressionSize</a>: <i>Double</i>
    <a href="#mode" title="Mode">Mode</a>: <i>String</i>
    <a href="#policy" title="Policy">Policy</a>: <i><a href="policy.md">Policy</a></i>
    <a href="#parameters" title="Parameters">Parameters</a>: <i><a href="parameters.md">Parameters</a></i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tags.md">Tags</a></i>
</pre>

## Properties

#### Id

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ApiKeySourceType

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### BinaryMediaTypes

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Body

_Required_: No

_Type_: <a href="body.md">Body</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### BodyS3Location

_Required_: No

_Type_: <a href="bodys3location.md">BodyS3Location</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### CloneFrom

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### EndpointConfiguration

_Required_: No

_Type_: <a href="endpointconfiguration.md">EndpointConfiguration</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Description

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### FailOnWarnings

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Name

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MinimumCompressionSize

_Required_: No

_Type_: Double

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Mode

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Policy

_Required_: No

_Type_: <a href="policy.md">Policy</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Parameters

_Required_: No

_Type_: <a href="parameters.md">Parameters</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Tags

_Required_: No

_Type_: List of <a href="tags.md">Tags</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the Id.
