resource "aws_iam_user" "workshop-1" {
  name = "workshop-1"
  path = "/workshop-1/"

  tags = {
    user = "eks-workshop-1"
  }
}