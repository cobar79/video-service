/*******************************************************************************
 * SBIR DATA RIGHTS
 *
 * Funding Agreement No. 21436572
 * Award Date: 1 Aug 2019
 * SBIR Protection Period: 1 Aug 2019 - 31 July 2044
 * FEDSIM Project No. 47QFCA21Z1045
 * Contract No. 47QFCA-19C-0016
 * SBIR Awardee: Bluestaq LLC
 * Contractor Address: 2 N Cascade Ave, Colorado Springs, CO 80903
 *
 * The Government's rights to use, modify, reproduce, release, perform, display,
 * or disclose technical data or computer software marked with this legend are
 * restricted during the period shown as provided in Article 12 of the listed
 * agreement-Small Business Innovation Research (SBIR) Data Rights. No
 * restrictions apply after the expiration date shown above. Any reproduction of
 * technical data, computer software, or portions thereof marked with this legend
 * must also reproduce the markings.
 *
 * Article 12 of the listed agreement states (in part) that the Government will
 * refrain from disclosing SBIR data outside the Government (except support
 * contractors working in an advice and assistance role) and especially to
 * competitors of the Contractor, or from using the information to produce
 * future technical procurement specifications that could harm the Contractor.
 ******************************************************************************/

package com.bar.osi.video.config.aws;

import java.io.File;

import com.bar.osi.video.service.exceptions.ErrorReason;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.profiles.ProfileFile;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class AwsCredentialsConfig implements InitializingBean {

	/** AWS Local Profile. */
	@Value(value = "${aws.profile: osi-local}")
	private String localProfile;

	/** AWS Test profile. */
	private static final String TEST_PROFILE = "localstack";

	@Override
	public void afterPropertiesSet() throws Exception {
		log.info(this.toString());
	}

	/**
	 * <dl>
	 *     <dt> <b>Logic</b> </dt>
	 *     <dd style='margin-top:10'> Try to build Instance Credentials (Role bound to AWS Service) </dd>
	 *     <dd style='margin-top:10'>
	 *         If unable to load credentials from IAM role, either running locally or AWS Service not set up correctly.
	 *     </dd>
	 *     <dd style='margin-top:10'>
	 *         If unable to load credentials from AWS profile file and IAM Role not attached to Service, throw runtime exception
	 *     </dd>
	 *     <dd style='margin-top:10'> Otherwise, return credential provider. </dd>
	 * </dl>
	 *
	 * @return AwsCredentialsProvider
	 */
	public AwsCredentialsProvider environmentCredentialProvider() {
		AwsCredentialsProvider credentialsProvider = InstanceProfileCredentialsProvider.create();
		if (!isCredentialProviderValid(credentialsProvider)) {
			credentialsProvider = ProfileCredentialsProvider.create(localProfile);
			log.debug("Validating credentials for {}", localProfile);
			if (!isCredentialProviderValid(credentialsProvider)) {
				throw new AwsCredentialsException("Valid AWS credentials unable to be found", ErrorReason.CONFIGURATION);
			}
		}
		return credentialsProvider;
	}

	public AwsCredentialsProvider testingCredentialProvider() {
		File credentialFile = new File("src/test/resources/localstack/credential.properties");
		AwsCredentialsProvider credentialsProvider = ProfileCredentialsProvider.builder()
			.profileFile(file -> file.type(ProfileFile.Type.CREDENTIALS).content(credentialFile.toPath()))
			.profileName(TEST_PROFILE)
			.build();

		if (isCredentialProviderValid(credentialsProvider)) {
			return credentialsProvider;
		} else {
			throw new AwsCredentialsException("Localstack credentials invalid", ErrorReason.CONFIGURATION);
		}
	}

	private static boolean isCredentialProviderValid(AwsCredentialsProvider awsCredentialsProvider) {
		boolean validCredentials;
		try {
			AwsCredentials awsCredentials = awsCredentialsProvider.resolveCredentials();
			// true if awsCredentialsProvider was able to provide an access key and secret access key
			log.debug("awsCredentials null? {}", awsCredentials == null);
			if (awsCredentials != null) {
				log.debug("key null? {}", awsCredentials.accessKeyId() == null);
				log.debug("secret null? {}", awsCredentials.secretAccessKey() == null);
			}
			validCredentials = awsCredentials != null && awsCredentials.accessKeyId() != null && awsCredentials.secretAccessKey() != null;
		} catch (Exception e) {
			log.warn(e.getMessage());
			validCredentials = false;
		}
		return validCredentials;
	}

	@Override
	public String toString() {
		return "AwsCredentialsConfig{" +
			"profile='" + localProfile + '\'' +
			'}';
	}

}
