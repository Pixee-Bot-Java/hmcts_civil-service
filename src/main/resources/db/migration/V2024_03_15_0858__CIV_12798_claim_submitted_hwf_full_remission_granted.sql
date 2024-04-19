/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimIssue.HWF.FullRemission',
        '{"Notice.AAA6.ClaimIssue.HWF.Requested", "Notice.AAA6.ClaimIssue.HWF.InvalidRef", "Notice.AAA6.ClaimIssue.HWF.InfoRequired", "Notice.AAA6.ClaimIssue.HWF.Updated" }',
        '{"Notice.AAA6.ClaimIssue.HWF.FullRemission": ["claimFee"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimIssue.HWF.FullRemission', 'Your help with fees application has been reviewed', 'Your help with fees application has been reviewed',
        '<p class="govuk-body">The full claim fee of ${claimFee} will be covered. You do not need to make a payment.</p>',
        '<p class="govuk-body">The full claim fee of ${claimFee} will be covered. You do not need to make a payment.</p>',
        'CLAIMANT');

