package com.carrotgarden.aws;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.CreateStackResult;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.Stack;

public class MakeCFN {

	static final Logger logger = LoggerFactory.getLogger(MakeCFN.class);

	public static void main(String[] args) throws Exception {

		final String name = "market-" + System.currentTimeMillis();

		logger.debug("init");

		// final String text = FileUtils.readFileToString(file);

		//

		final String home = System.getProperty("user.home");

		final File file = new File(home,
				".amazon/carrotgarden/credentials/admin-cfn.properties");

		logger.debug("file" + file);

		final AWSCredentials props = new PropertiesCredentials(file);

		final AmazonCloudFormation client = new AmazonCloudFormationClient(
				props);

		final CreateStackRequest request = new CreateStackRequest();

		request.setStackName(name);

		final File body = new File(
				"./src/main/resources/carrotgarden/ec2-market.template");

		final String template = FileUtils.readFileToString(body);

		request.setTemplateBody(template);

		final CreateStackResult result = client.createStack(request);

		logger.debug("result=" + result);

		//

		for (int index = 0; index < 1000; index++) {

			final DescribeStacksRequest describeRequest = new DescribeStacksRequest();

			describeRequest.setStackName(name);

			final DescribeStacksResult describeResult = client
					.describeStacks(describeRequest);

			final Stack stack = describeResult.getStacks().get(0);

			final String status = stack.getStackStatus();

			logger.debug("index=" + index);
			logger.debug("status=" + status);

			Thread.sleep(1000 * 5);

		}

		//

		logger.debug("done");

	}

}
