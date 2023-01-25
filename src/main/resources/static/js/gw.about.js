/**
 * 
 * About Geoweaver Page
 * 
 */

GW.about = {
		
		dependency: "d3.js, bootstrap, jquery, codemirror, directed-graph-creator, dmuploader",
		
		content: "thanks to Colorado Reed (https://github.com/cjrd) for making the fantastic D3.js graph creator.",
		
		showDialog: function(){
			
			var content = '<div style=\"padding:10px\">'+
					'<p class=\"text-left\">Geoweaver is a web system to allow users to easily compose and execute full-stack deep learning workflows in web browsers by ad hoc integrating the distributed spatial data facilities, high-performance computation platforms, and open-source deep learning libraries.</p>'+
					
					'<p class=\"text-left\" >Geoweaver (version '+ GW.version+') is currently supported by ESIPLab, <a href=\"https://www.nsf.gov/awardsearch/showAward?AWD_ID=1947893&HistoricalAwards=false\">NSF geoinformatics program #1947893 and #1947875</a> and '+
					'<a href=\"https://earthdata.nasa.gov/esds/competitive-programs/access/geoweaver\">NASA ACCESS-19</a>. The source code is on <a href=\"http://github.com/ESIPFed/Geoweaver\">Github</a>.  </p>'+
	            	
	            	'<p class=\"text-left\">Geoweaver logo is designed by Dr. Annie Burgess <a href=\"mailto:annieburgess@esipfed.org\">contact</a>.</p>'+
	            	
	            	'<p class=\"text-left\">Geoweaver is a community effort and welcome all contributors. If you have any questions, please create a new issue in GitHub or directly <a href=\"mailto:zsun@Geoweaver provider.edu\">contact us</a></p></div>';
			
					
			GW.process.createJSFrameDialog(720, 640, content, "About");
			
		},

		showTerms: function(){

			var content = `
			
			<div style="margin: 10px; font-size: 14px;">
			<p>1. Scope</p>
			<p>1.1. Any use of this software provided by Geoweaver developer or maintanence team (Geoweaver provider), is subject to these Terms of Use. These Terms of Use may be amended, modified or replaced by other terms and conditions, e.g . for the purchase of products and services. With log-in, or where a log-in is not required, in accessing or using the Geoweaver software these Terms of Use are accepted in their then current version.</p>
			<p>1.2. In the case of Web offers aimed at companies or public enterprises, such companies or enterprises are represented by the user and must assume that the user has appropriate knowledge and acts accordingly.</p>

			<p>2. Services</p>
			<p>2.1. This Geoweaver software contains specific information and software, as well as - as the case may be - related documentation, for viewing or downloading.</p>
			<p>2.2. Geoweaver provider may stop the operation of the Geoweaver software in full or in part at any time. Due to the nature of the internet and computer systems, Geoweaver provider cannot accept any liability for the continuous availability of the Geoweaver software.</p>

			<p>3. Registration, Password</p>
			<p>3.1. Some pages of the Geoweaver software may be password protected. In the interest of safety and security of the business transactions, only registered Users may access said pages. Geoweaver provider reserves the right to deny registration to any User. Geoweaver provider particularly reserves the right to determine certain sites, which were previously freely accessible, subject to registration. Geoweaver provider is entitled, at any time and without obligation to give reasons, to deny the User the right to access the password-protected area by blocking its User Data (as defined below), in particular if the User </p>
			<p>- uses false data for the purpose of registration;</p>
			<p>- violates these Terms of Use or neglects its duty of care with regard to User Data; </p>
			<p>- violates any applicable laws in the access to or use of the Geoweaver software; or </p>
			<p>- did not use the Geoweaver software for a longer period.</p>
			<p>3.2. For registration the User shall give accurate information and, where such information changes over time, update such information (to the extent possible: online) without undue delay. The User shall ensure that its e-mail address, as supplied to Geoweaver provider, is current at all times and an address at which the User can be contacted.</p>
			<p>3.3. Upon registration the User will be provided with an access code, comprising a User ID and a password ("User Data"). On first access the User shall promptly change the password received from Geoweaver provider into a password known only to the User. The User Data allows the User to view or change its data or, as applicable, to withdraw its consent to data processing.</p>
			<p>3.4. The User shall ensure that User Data is not accessible by third parties and is liable for all transactions and other activities carried out under its User Data. At the end of each online session, the User shall log-off from the password protected websites. If and to the extent the User becomes aware that third parties are misusing its User Data the User shall notify Geoweaver provider thereof without undue delay in writing, or, as the case may be, by e-mail.</p>
			<p>3.5. After receipt of the notice under paragraph 3.4, Geoweaver provider will deny access to the password-protected area under such User Data. Access by the User will only be possible again upon the User's application to Geoweaver provider or upon new registration.</p>
			<p>3.6. The User may at any time request termination of its registration in writing, provided that the deletion will not violate the proper performance of contractual relationships. In such event Geoweaver provider will remove all user data and other stored personally identifiable data of the User as soon as these data are no longer needed.</p>

			<p>4. Rights of Use to Information, Software and Documentation</p>
			<p>4.1. The use of any information, software and documentation made available on or via this Geoweaver software is subject to these Terms of Use or, in case of updating information, software or documentation, subject to the applicable license terms previously agreed to with Geoweaver provider. Separately agreed to license terms, for example software downloads, shall prevail over these Terms of Use.</p>
			<p>4.2. Geoweaver provider grants User a non-exclusive and non-transferable license, which may not be sublicensed, to use the information, software and documentation made available to the User on or via the Geoweaver software to the extent agreed, or in the event of no such agreement to the extent of the purpose intended by Geoweaver provider in making same available.</p>
			<p>4.3. Software shall be made available at no expense in object code. There shall be no right for the source code to be made available. This shall not apply to source code related to open source software, which license conditions take priority over these Terms of Use in the case of transfer of open source software and which conditions require the making available of the source code. In such case Geoweaver provider shall make the source code available in return for the payment of costs.</p>
			<p>4.4. Information, software and documentation may not be distributed by the User to any third party at any time nor may it be rented or in any other way made available. Unless such is allowed by mandatory law, the User shall not modify the software or documentation nor shall it disassemble, reverse engineer or decompile the software or separate any part thereof. The User may make one backup copy of the software where necessary to secure further use in accordance with these Terms of Use.</p>
			<p>4.5. The information, software and documentation are protected by copyright laws as well as international copyright treaties as well as other laws and conventions related to intellectual property. The User shall observe such laws and in particular shall not modify, conceal or remove any alphanumeric code, marks or copyright notices neither from the information nor from the software or documentation, or any copies thereof.</p>

			<p>5. Intellectual Property</p>
			<p>5.1. Notwithstanding the particular provisions in § 4 of these Terms of Use, information, brand names and other contents of the Geoweaver software may not be changed, copied, reproduced, sold, rented, used, supplemented or otherwise used in any other way without the prior written permission of Geoweaver provider.</p>
			<p>5.2. Except for the rights of use and other rights expressly granted herein, no other rights are granted to the User nor shall any obligation be implied requiring the grant of further rights. Any and all patent rights and licenses are expressly excluded.</p>
			<p>5.3. Geoweaver provider may, without charge, use any ideas or proposals stored by a User on the Geoweaver softwares for the development, improvement and sale of its products.</p>

			<p>6. Duties of the User</p>
			<p>6.1. In accessing or using the Geoweaver software the User shall not </p>
			<p>- harm other persons, in particular minors, or infringe their personal rights; </p>
			<p>- breach public morality in its manner of use; </p>
			<p>- violate any intellectual property right or any other proprietary right; </p>
			<p>- upload any contents containing a virus, so-called Trojan Horse, or any other program that could damage data; </p>
			<p>- transmit, store or upload hyperlinks or contents to which the User is not entitled, in particular in cases where such hyperlinks or contents are in breach of confidentiality obligations or unlawful; or </p>
			<p>- distribute advertising or unsolicited e-mails (so-called "spam") or inaccurate warnings of viruses, defects or similar material and the User shall not solicit or request the participation in any lottery, snowball system, chain letter, pyramid game or similar activity.</p>
			<p>6.2. Geoweaver provider may deny access to the Geoweaver software at any time, in particular if the User breaches any obligation arising from these Terms of Use.</p>

			<p>7. Hyperlinks</p>
			<p>The Geoweaver software may contain hyperlinks to the web pages of third parties. Geoweaver provider shall have no liability for the contents of such web pages and does not make representations about or endorse such web pages or their contents as its own, as Geoweaver provider does not control the information on such web pages and is not responsible for the contents and information given thereon. The use of such web pages shall be at the sole risk of the User.</p>

			<p>8. Liability for defects of title or quality</p>
			<p>8.1. Insofar as any information, software or documentation is made available at no cost, any liability for defects as to quality or title of the information, software and documentation especially in relation to the correctness or absence of defects or the absence of claims or third party rights or in relation to completeness and/or fitness for purpose are excluded except for cases involving willful misconduct or fraud.</p>
			<p>8.2. The information on the Geoweaver software may contain specifications or general descriptions related to the technical possibilities of individual products which may not be available in certain cases (e.g. due to product changes). The required performance of the product shall therefore be mutually agreed in each case at the time of purchase.</p>

			<p>9. Other Liability, Viruses</p>
			<p>9.1. The liability of Geoweaver provider for defects in relation to quality and title shall be determined in accordance with the provisions of § 8 of these Terms of Use. Any further liability of Geoweaver provider is excluded unless required by law, e.g. under the Act on Product Liability or in cases of willful misconduct, gross negligence, personal injury or death, failure to meet guaranteed characteristics, fraudulent concealment of a defect or in case of breach of fundamental contractual obligations. The damages in case of breach of fundamental contractual obligations is limited to the contract-typical, foreseeable damage if there is no willful misconduct or gross negligence.</p>
			<p>9.2. Although Geoweaver provider makes every endeavor to keep the Geoweaver software free from viruses, Geoweaver provider cannot make any guarantee that it is virus-free. The User shall, for its own protection, take the necessary steps to ensure appropriate security measures and shall utilize a virus scanner before downloading any information, software or documentation.</p>
			<p>9.3. §§ 9.1 and 9.2 do not intend nor imply any changes to the burden of proof to the User's disadvantage.</p>

			<p>10. Compliance with Export Control Regulations</p>
			<p>10.1. If the User transfers information, software and documentation provided by Geoweaver provider to a third party, the User shall comply with all applicable national and international (re-) export control regulations. In any event of such transfer the User shall comply with the (re-) export control regulations of the United States of America, of the European Union and of the United States of America.</p>
			<p>10.2. Prior to any such transfer to a third party the User shall in particular check and guarantee by appropriate measures that </p>
			<p>- There will be no infringement of an embargo imposed by the European Union, by the United States of America and/ or by the United Nations by such transfer or by provision of other economic resources in connection with information, software and documentation provided by Geoweaver provider, also considering the limitations of domestic business and prohibitions of by-passing those embargos; </p>
			<p>- Such information, software and documentation provided by Geoweaver provider are not intended for use in connection with armaments, nuclear technology or weapons, if and to the extent such use is subject to prohibition or authorization, unless required authorization is provided; </p>
			<p>- The regulations of all applicable Sanctioned Party Lists of the European Union and the United States of America concerning the trading with entities, persons and organizations listed therein are considered.</p>
			<p>10.3. If required to enable authorities or Geoweaver provider to conduct export control checks, the User, upon request by Geoweaver provider, shall promptly provide Geoweaver provider with all information pertaining to the particular end-user, the particular destination and the particular intended use of information, software and documentation provided by Geoweaver provider, as well as any export control restrictions existing.</p>
			<p>10.4. The User shall indemnify and hold harmless Geoweaver provider from and against any claim, proceeding, action, fine, loss, cost and damages arising out of or relating to any noncompliance with export control regulations by the User, and the User shall compensate Geoweaver provider for all losses and expenses resulting thereof, unless such noncompliance was not caused by fault of the User. This provision does not imply a change in burden of proof.</p>
			<p>10.5. Geoweaver provider' obligation to fulfill an agreement is subject to the proviso that the fulfillment is not prevented by any impediments arising out of national and international foreign trade and customs requirements or any embargos or other sanctions.</p>

			<p>11. Data Privacy Protection</p>
			<p>For collection, use and processing of personally identifiable data of the User of the Geoweaver software, Geoweaver provider shall comply with applicable laws on data privacy protection and the Geoweaver software Data Protection Privacy Policy, which is available per hyperlink on the Geoweaver software.</p>

			<p>12. Supplementary Agreements, Place of Jurisdiction, Applicable Law</p>
			<p>12.1. Any supplementary agreement requires the written form.</p>
			<p>12.2. The individual pages of the Geoweaver software are normally self operated and administered by Geoweaver users. The pages comply with the law applicable in the country where the responsible company has its business residence. Geoweaver provider makes no representation that information, software and/or documentation on the Geoweaver software are appropriate or available for viewing or downloading at locations outside such country. If Users access Geoweaver software from outside such country, they are exclusively responsible for compliance with all applicable local laws. Access to Geoweaver software's information, software and/or documentation from countries where such content is unlawful is prohibited. In this case and where User seeks to do business with Geoweaver provider, the User should contact the Geoweaver provider representative for the particular country for country specific business.</p>
			<p>12.3. These Terms of Use shall be governed by - and all disputes relating to or in connection with these Terms of Use or their subject matter shall be resolved in accordance with - the laws of U.S.A, to the exclusion of its conflict of laws rules. The application of the United Nations Convention on Contracts for the International Sales of Goods (CISG) of 11 April 1980 is excluded.</p>
			</div>]`

			GW.process.createJSFrameDialog(720, 640, content, "User Terms");

		}
		
}