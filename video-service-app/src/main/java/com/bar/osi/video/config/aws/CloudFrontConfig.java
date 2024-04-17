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

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "aws.cloud-front")
public class CloudFrontConfig {

	private String cachePolicyId;
}
