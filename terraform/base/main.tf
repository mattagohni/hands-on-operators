data "aws_iam_user" "workshop-1" {
  user_name = "workshop-1"
}

resource "aws_iam_role" "eks_role" {
  name               = "eks-access-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Action = "sts:AssumeRole",
        Effect = "Allow",
        Principal = {
          "Service": "eks.amazonaws.com"
        },
      },
    ],
  })
}

resource "aws_iam_user_policy_attachment" "workshop-user-attachment" {
  policy_arn = aws_iam_policy.eks_policy.arn
  user       = data.aws_iam_user.workshop-1.user_name
}

resource "aws_iam_policy" "eks_policy" {
  name        = "eks-cluster-access-policy"
  description = "Policy allowing access to the EKS cluster"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect   = "Allow"
        "Action": [
          "eks:DescribeNodegroup",
          "eks:ListNodegroups",
          "eks:DescribeCluster",
          "eks:ListClusters",
          "eks:AccessKubernetesApi",
          "ssm:GetParameter",
          "eks:ListUpdates",
          "eks:ListFargateProfiles"
        ],
        Resource = "*"
      },
      {
        Effect   = "Allow"
        Action   = "eks:AccessKubernetesApi"
        Resource = "*"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "eks_policy_attachment" {
  role       = aws_iam_role.eks_role.name
  policy_arn = aws_iam_policy.eks_policy.arn
}
