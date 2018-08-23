package controllers.v1.api.examples.unitlinks

import io.swagger.annotations.ApiModelProperty

@deprecated(message = "This is just for Swagger example purposes - do not use in real code", since = "")
case class TestOperationForSwagger(
  @ApiModelProperty(value = "the operation to be applied to the target resource at the location specified by path", dataType = "string", example = "test", required = true) op: String,
  @ApiModelProperty(value = "the path at which the operation should be applied", dataType = "string", example = "/parents/LEU", required = true) path: String,
  @ApiModelProperty(value = "the value to be applied to the operation", dataType = "string", example = "1234567890123456", required = true) value: String
)
