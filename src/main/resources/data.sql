INSERT INTO application_master (ID, APPLICATION_NAME) VALUES ('1', 'FIRSTGEN');
INSERT INTO application_master (ID, APPLICATION_NAME) VALUES ('2', 'ALM');
INSERT INTO application_master (ID, APPLICATION_NAME) VALUES ('3', 'ILONGUE');
INSERT INTO application_master (ID, APPLICATION_NAME) VALUES ('4', '64VB');

insert into rpa_upload_file_details (id,bank_name,process_name,folder_desc,folder_path) values (1,'HDFC Bank', '64VB Compliance', 'Bank file', '/rpa_upload/hdfc/bank/vb64/');
insert into rpa_upload_file_details (id,bank_name,process_name,folder_desc,folder_path) values (2,'AXIS Bank', '64VB Compliance', 'Bank file', '/rpa_upload/axis/bank/vb64/');
insert into rpa_upload_file_details (id,bank_name,process_name,folder_desc,folder_path) values (3,'HSBC Bank', '64VB Compliance', 'Bank file', '/rpa_upload/hsbc/bank/vb64/');
insert into rpa_upload_file_details (id,bank_name,process_name,folder_desc,folder_path) values (4,'CITI Bank', '64VB Compliance', 'Bank file', '/rpa_upload/citi/bank/vb64/');
insert into rpa_upload_file_details (id,bank_name,process_name,folder_desc,folder_path) values (5,'SCB Bank', '64VB Compliance', 'Bank file', '/rpa_upload/scb/bank/vb64/');

insert INTO rpa_user(id,username,password,email_id, phone_no,active,first_time_password)
VALUES (rpa_user_seq.NEXTVAL,'rpaadmin','$2a$10$iECeYZM5Awxahcu6Xp/7f.a0pHv41bPgZfonBBODTUpcCLIRroH7a', 'dominic.prabhu@tekplay.com', '8807002055', 'S','N');

insert into business_process (id,process_desc, process_name, process_state) values(1,'64VB Compliance for HDFC', 'HDFC_64VB_Compliance',0);
insert into business_process (id,process_desc, process_name, process_state) values(2,'64VB Compliance for HSBC', 'HSBC_64VB_Compliance',0);
insert into business_process (id,process_desc, process_name, process_state) values(3,'64VB Compliance for AXIS', 'AXIS_64VB_Compliance',0);
insert into business_process (id,process_desc, process_name, process_state) values(4,'64VB Compliance for CITI', 'CITI_64VB_Compliance',0);
insert into business_process (id,process_desc, process_name, process_state) values(5,'64VB Compliance for SCB', 'SCB_64VB_Compliance',0);
insert into business_process (id,process_desc, process_name, process_state) values(6,'Batch Process For GL Posting', 'FirstGenBatchProcess',0);
insert into business_process (id,process_desc, process_name, process_state) values(7,'Ilongue - Lifeline Migration', 'LifelineMigrationProcess',0);
insert into business_process (id,process_desc, process_name, process_state) values(8,'Ilongue - Lifeline Inward Upload', 'IlongueLifelineInwardUpload',0);

insert into business_process (id,process_desc, process_name, process_state) values(9,'Car Policy Extractor For Maruti', 'policyExtractionMaruti',0);
insert into business_process (id,process_desc, process_name, process_state) values(10,'Car Policy Extractor For Maruti Backlog', 'policyExtractionMarutiBacklog',0);
insert into business_process (id,process_desc, process_name, process_state) values(11,'Grid Posting Process', 'GridMasterUploadProcess',0);


INSERT INTO rpa_role (id, name)
VALUES (1, 'ROLE_ADMIN');
INSERT INTO rpa_role (id, name)
VALUES (2, 'ROLE_USER');

INSERT INTO rpa_user_role (user_id, role_id)
VALUES (1, 1);


INSERT INTO rpa_user_process (user_id, process_id)
VALUES (1, 1);
INSERT INTO rpa_user_process (user_id, process_id)
VALUES (1, 2);
INSERT INTO rpa_user_process (user_id, process_id)
VALUES (1, 3);
INSERT INTO rpa_user_process (user_id, process_id)
VALUES (1, 4);
INSERT INTO rpa_user_process (user_id, process_id)
VALUES (1, 5);
INSERT INTO rpa_user_process (user_id, process_id)
VALUES (1, 6);
INSERT INTO rpa_user_process (user_id, process_id)
VALUES (1, 7);
INSERT INTO rpa_user_process (user_id, process_id)
VALUES (1, 8);
INSERT INTO rpa_user_process (user_id, process_id)
VALUES (1, 9);
INSERT INTO rpa_user_process (user_id, process_id)
VALUES (1, 10);
INSERT INTO rpa_user_process (user_id, process_id)
VALUES (1, 11);



insert into folder_configuration_master (id,process_name,customer_name,file_type) values (1, '64VB Compliance','HDFC', 'Bank file');
insert into folder_configuration_master (id,process_name,customer_name,file_type) values (2, '64VB Compliance','HSBC', 'Bank file');
insert into folder_configuration_master (id,process_name,customer_name,file_type) values (3, '64VB Compliance','CITI', 'Bank file');
insert into folder_configuration_master (id,process_name,customer_name,file_type) values (4, '64VB Compliance','SCB', 'Bank file');