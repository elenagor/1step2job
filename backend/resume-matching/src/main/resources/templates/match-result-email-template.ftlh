<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta name="viewport" content="width=device-width, initial-scale=1.0" />
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>1step2job</title>
	<style type="text/css" rel="stylesheet" media="all">		/* Base ------------------------------ */
		
		*:not(br):not(tr):not(html) {
		    font-family: Arial, 'Helvetica Neue', Helvetica, sans-serif;
		    box-sizing: border-box;
		}
		
		body {
		    width: 100% !important;
		    height: 100%;
		    margin: 0;
		    line-height: 1.4;
		    background-color: #FFFFFF;
		    color: #464646;
		    -webkit-text-size-adjust: none;
		}
		
		p,
		ul,
		ol,
		blockquote {
		    line-height: 1.4;
		    text-align: left;
		}
		
		a {
		    color: #007d8a;
		    text-decoration: none;
		}
		
		    a img {
		        border: none;
		    }
		
		td {
		    word-break: break-word;
		}
		
		/* Layout ------------------------------ */
		
		.email-wrapper {
		    width: 100%;
		    height: 100%;
		    margin: 0;
		    padding: 0;
		    -premailer-width: 100%;
		    -premailer-cellpadding: 0;
		    -premailer-cellspacing: 0;
		    background-color: #F2F4F6;
		}
		
		.email-content {
		    width: 100%;
		    margin: 0;
		    padding: 0;
		    -premailer-width: 100%;
		    -premailer-cellpadding: 0;
		    -premailer-cellspacing: 0;
		}
		
		/* Masthead ----------------------- */
		
		.email-masthead {
		    padding: 20px 8px;
		    text-align: center;
		    background-color: #464646;
		}
		
		.email-masthead_logo {
		    float: left;
		}
		.email-masthead_title {
		    font-family: 'Tw Cen MT', 'Twentieth Century', Arial, sans-serif;
		    height: 60px;
		    float: left;
		    margin: 30px 8px 0 5px;
		    color: #FFFFFF;
		    font-size: 40px;
		}
		.email-head-footer {
		    width: 100%;
		    height: 4px;
		    margin: 0 auto;
		    padding: 0;
		    text-align: center;
		    background-color: #766699;
		}
		
		/* Body ------------------------------ */
		
		.email-body {
		    width: 100%;
		    margin: 0;
		    padding: 0;
		    border-top: 1px solid #EDEFF2;
		    border-bottom: 1px solid #EDEFF2;
		}
		
		.email-body_inner {
		    width: 100%;
		    margin: 0 auto;
		    padding: 0;
		    margin-bottom: 30px;
		    background-color: #FFFFFF;
		}
		
		.email-footer {
		    width: 100%;
		    height: 46px;
		    margin: 0 auto;
		    padding: 0;
		    text-align: center;
		    background-color: #766699;
		}
		
		    .email-footer p {
		        color: #FFFFFF;
		    }
		
		.body-action {
		    width: 100%;
		    margin: 30px auto;
		    padding: 0;
		    text-align: center;
		}
		
		.body-sub {
		    margin-top: 25px;
		    padding-top: 25px;
		    border-top: 1px solid #EDEFF2;
		}
		
		.content-cell {
		    padding: 16px;
		}
		
		
		/*Media Queries ------------------------------ */
		
		@@media only screen and (max-width: 600px) {
		    .email-body_inner,
		    .email-footer {
		        width: 100% !important;
		    }
		}
		
		@@media only screen and (max-width: 500px) {
		    .button {
		        width: 100% !important;
		    }
		}
		
		/* Buttons ------------------------------ */
		
		.button {
		    color: #007d8a;
		    border: none;
		    display: block;
		    font-weight: bold;
		    text-decoration: underline;
		    -webkit-text-size-adjust: none;
		}
		
		/* Utilities*/
		
		.align-center {
		    text-align: center;
		}
		
		/* Type ------------------------------ */
		
		h1 {
		    margin-top: 0;
		    color: #007d8a;
		    font-size: 19px;
		    font-weight: bold;
		    text-align: left;
		}
		
		h2 {
		    margin-top: 0;
		    color: #2F3133;
		    font-size: 16px;
		    font-weight: bold;
		    text-align: left;
		}
		
		h3 {
		    margin-top: 0;
		    color: #2F3133;
		    font-size: 14px;
		    font-weight: bold;
		    text-align: left;
		}
		
		p {
		    margin-top: 0;
		    color: #464646;
		    font-size: 16px;
		    line-height: 1.5em;
		    text-align: left;
		}
		
		    p.sub {
		        font-size: 12px;
		    }
		
		    p.center {
		        text-align: center;
		    }
	</style>
</head>
<body>
<table class="email-wrapper" width="100%" height="100%" cellpadding="0" cellspacing="0">
	<tr>
		<td align="center"> 
		<table class="email-content" width="100%" cellpadding="0" cellspacing="0">
			<tr>
				<td class="email-head-footer" />
			</tr>
<!-- Email Body -->
			<tr>
				<td class="email-body" width="100%" cellpadding="0" cellspacing="0"> 
				<table class="email-body_inner" align="center" width="100%" cellpadding="0" cellspacing="0">
<!-- Body content -->
					<tr>
						<td class="content-cell"> 
						<p>
						<#if person_name??>Hello ${person_name} ! <#else> Hello </#if>
						<br>
							<#if job_title??>We found for you a job: ${job_title} ! <#else>   </#if>
							<#if published_date??> published on ${published_date} ! <#else>   </#if>
						</br>
						<br>
							<#if job_description??> ${job_description} <#else> . </#if>
						</br>
						<br>
							<#if overall_score??> score matching = ${overall_score} <#else>  </#if>
							<#if apply_url??>to apply click <a href="${apply_url}">here</a><#else>  </#if>
						</br>
						</p>
						</td>
					</tr>
				</table>
				</td>
			</tr>
			<tr>
				<td> 
				<table class="email-footer" align="center" width="570" cellpadding="0" cellspacing="0">
					<tr>
						<td class="content-cell" align="center"> 
						<p class="sub align-center">
							&copy; 2025 1Step2job LLC. All rights reserved.
						</p>
						</td>
					</tr>
				</table>
				</td>
			</tr>
		</table>
		</td>
	</tr>
</table>
</body>
</html>
