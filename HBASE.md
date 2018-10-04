## HBase REST Instructions

Firstly, start HBase.

```shell
start-hbase.sh
```

Then start HBase REST.

```shell
hbase rest start
```

We now need to insert some test data into HBase.

Open the HBase shell.

```shell
hbase shell
```

Create the namespace.

```shell
create_namespace 'sbr_control_db'
```

Create the tables within the namespace with a column family.
Note that there are distinct tables per period.

```shell
create 'sbr_control_db:enterprise_201801', 'd'
create 'sbr_control_db:legal_unit_201801', 'd'
create 'sbr_control_db:local_unit_201801', 'd'
create 'sbr_control_db:reporting_unit_201801', 'd'
create 'sbr_control_db:unit_link_201801', 'l'

create 'sbr_control_db:enterprise_201802', 'd'
create 'sbr_control_db:legal_unit_201802', 'd'
create 'sbr_control_db:local_unit_201802', 'd'
create 'sbr_control_db:reporting_unit_201802', 'd'
create 'sbr_control_db:unit_link_201802', 'l'
```

Insert some Enterprise data.

```shell
put 'sbr_control_db:enterprise_201801', '3210000001', 'd:ern', '1000000123'
put 'sbr_control_db:enterprise_201801', '3210000001', 'd:entref', '201801-entref-1000000123'
put 'sbr_control_db:enterprise_201801', '3210000001', 'd:name', '201801-name-1000000123'
put 'sbr_control_db:enterprise_201801', '3210000001', 'd:address1', '201801-addresss1-1000000123'
put 'sbr_control_db:enterprise_201801', '3210000001', 'd:address2', '201801-address2-1000000123'
put 'sbr_control_db:enterprise_201801', '3210000001', 'd:address3', '201801-address3-1000000123'
put 'sbr_control_db:enterprise_201801', '3210000001', 'd:address4', '201801-address4-1000000123'
put 'sbr_control_db:enterprise_201801', '3210000001', 'd:address5', '201801-address5-1000000123'
put 'sbr_control_db:enterprise_201801', '3210000001', 'd:postcode', '201801-postcode-1000000123'
put 'sbr_control_db:enterprise_201801', '3210000001', 'd:sic07', '201801-sic07-1000000123'
put 'sbr_control_db:enterprise_201801', '3210000001', 'd:trading_style', '201801-tradingstyle-1000000123'
put 'sbr_control_db:enterprise_201801', '3210000001', 'd:legal_status', '201801-legalstatus-1000000123'
put 'sbr_control_db:enterprise_201801', '3210000001', 'd:paye_jobs', '5'
put 'sbr_control_db:enterprise_201801', '3210000001', 'd:paye_empees', '1'
put 'sbr_control_db:enterprise_201801', '3210000001', 'd:cntd_turnover', '99'
put 'sbr_control_db:enterprise_201801', '3210000001', 'd:std_turnover', '100'
put 'sbr_control_db:enterprise_201801', '3210000001', 'd:grp_turnover', '101'
put 'sbr_control_db:enterprise_201801', '3210000001', 'd:app_turnover', '102'
put 'sbr_control_db:enterprise_201801', '3210000001', 'd:ent_turnover', '103'
put 'sbr_control_db:enterprise_201801', '3210000001', 'd:prn', '0.038709584'
put 'sbr_control_db:enterprise_201801', '3210000001', 'd:working_props', '183'
put 'sbr_control_db:enterprise_201801', '3210000001', 'd:employment', '34'
put 'sbr_control_db:enterprise_201801', '3210000001', 'd:region', '201801-region-1'

put 'sbr_control_db:enterprise_201802', '3210000001', 'd:ern', '1000000123'
put 'sbr_control_db:enterprise_201802', '3210000001', 'd:entref', '201802-entref-1000000123'
put 'sbr_control_db:enterprise_201802', '3210000001', 'd:name', '201802-name-1000000123'
put 'sbr_control_db:enterprise_201802', '3210000001', 'd:address1', '201802-addresss1-1000000123'
put 'sbr_control_db:enterprise_201802', '3210000001', 'd:address2', '201802-address2-1000000123'
put 'sbr_control_db:enterprise_201802', '3210000001', 'd:address3', '201802-address3-1000000123'
put 'sbr_control_db:enterprise_201802', '3210000001', 'd:address4', '201802-address4-1000000123'
put 'sbr_control_db:enterprise_201802', '3210000001', 'd:address5', '201802-address5-1000000123'
put 'sbr_control_db:enterprise_201802', '3210000001', 'd:postcode', '201802-postcode-1000000123'
put 'sbr_control_db:enterprise_201802', '3210000001', 'd:sic07', '201802-sic07-1000000123'
put 'sbr_control_db:enterprise_201802', '3210000001', 'd:trading_style', '201802-tradingstyle-1000000123'
put 'sbr_control_db:enterprise_201802', '3210000001', 'd:legal_status', '201802-legalstatus-1000000123'
put 'sbr_control_db:enterprise_201802', '3210000001', 'd:paye_jobs', '5'
put 'sbr_control_db:enterprise_201802', '3210000001', 'd:paye_empees', '1'
put 'sbr_control_db:enterprise_201802', '3210000001', 'd:cntd_turnover', '99'
put 'sbr_control_db:enterprise_201802', '3210000001', 'd:std_turnover', '100'
put 'sbr_control_db:enterprise_201802', '3210000001', 'd:grp_turnover', '101'
put 'sbr_control_db:enterprise_201802', '3210000001', 'd:app_turnover', '102'
put 'sbr_control_db:enterprise_201802', '3210000001', 'd:ent_turnover', '103'
put 'sbr_control_db:enterprise_201802', '3210000001', 'd:prn', '0.038709584'
put 'sbr_control_db:enterprise_201802', '3210000001', 'd:working_props', '183'
put 'sbr_control_db:enterprise_201802', '3210000001', 'd:employment', '34'
put 'sbr_control_db:enterprise_201802', '3210000001', 'd:region', '201802-region-1'
```

Insert some Legal Unit data.

```shell
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:ubrn', '1000012345000002'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:name', '201801-name-1000012345000002'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:trading_style', '201801-tradingstyle-1000012345000002'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:address1', '201801-address1-1000012345000002'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:address2', '201801-address2-1000012345000002'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:address3', '201801-address3-1000012345000002'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:address4', '201801-address4-1000012345000002'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:address5', '201801-address5-1000012345000002'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:postcode', '201801-postcode-1000012345000002'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:sic07', '201801-sic07-1000012345000002'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:paye_jobs', '2'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:turnover', '20'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:legal_status', '201801-legalstatus-1000012345000002'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:trading_status', '201801-tradingstatus-1000012345000002'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:birth_date', '201801-birthdate-1000012345000002'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:death_date', '201801-deathdate-1000012345000002'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:death_code', '201801-deathcode-1000012345000002'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:crn', '23847563'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:uprn', '201801-uprn-1000012345000002'

put 'sbr_control_db:legal_unit_201802', '3210000001~1000012345000002', 'd:ubrn', '1000012345000002'
put 'sbr_control_db:legal_unit_201802', '3210000001~1000012345000002', 'd:name', '201802-name-1000012345000002'
put 'sbr_control_db:legal_unit_201802', '3210000001~1000012345000002', 'd:trading_style', '201802-tradingstyle-1000012345000002'
put 'sbr_control_db:legal_unit_201802', '3210000001~1000012345000002', 'd:address1', '201802-address1-1000012345000002'
put 'sbr_control_db:legal_unit_201802', '3210000001~1000012345000002', 'd:address2', '201802-address2-1000012345000002'
put 'sbr_control_db:legal_unit_201802', '3210000001~1000012345000002', 'd:address3', '201802-address3-1000012345000002'
put 'sbr_control_db:legal_unit_201802', '3210000001~1000012345000002', 'd:address4', '201802-address4-1000012345000002'
put 'sbr_control_db:legal_unit_201802', '3210000001~1000012345000002', 'd:address5', '201802-address5-1000012345000002'
put 'sbr_control_db:legal_unit_201802', '3210000001~1000012345000002', 'd:postcode', '201802-postcode-1000012345000002'
put 'sbr_control_db:legal_unit_201802', '3210000001~1000012345000002', 'd:sic07', '201802-sic07-1000012345000002'
put 'sbr_control_db:legal_unit_201802', '3210000001~1000012345000002', 'd:paye_jobs', '2'
put 'sbr_control_db:legal_unit_201802', '3210000001~1000012345000002', 'd:turnover', '20'
put 'sbr_control_db:legal_unit_201802', '3210000001~1000012345000002', 'd:legal_status', '201802-legalstatus-1000012345000002'
put 'sbr_control_db:legal_unit_201802', '3210000001~1000012345000002', 'd:trading_status', '201802-tradingstatus-1000012345000002'
put 'sbr_control_db:legal_unit_201802', '3210000001~1000012345000002', 'd:birth_date', '201802-birthdate-1000012345000002'
put 'sbr_control_db:legal_unit_201802', '3210000001~1000012345000002', 'd:death_date', '201802-deathdate-1000012345000002'
put 'sbr_control_db:legal_unit_201802', '3210000001~1000012345000002', 'd:death_code', '201802-deathcode-1000012345000002'
put 'sbr_control_db:legal_unit_201802', '3210000001~1000012345000002', 'd:crn', '23847563'
put 'sbr_control_db:legal_unit_201802', '3210000001~1000012345000002', 'd:uprn', '201802-uprn-1000012345000002'
```

Insert some Local Unit data.

```shell
put 'sbr_control_db:local_unit_201801', '3210000001~900000123', 'd:lurn', '900000123'
put 'sbr_control_db:local_unit_201801', '3210000001~900000123', 'd:luref', '201801-luref-900000123'
put 'sbr_control_db:local_unit_201801', '3210000001~900000123', 'd:ern', '1000000123'
put 'sbr_control_db:local_unit_201801', '3210000001~900000123', 'd:entref', '201801-entref-900000123'
put 'sbr_control_db:local_unit_201801', '3210000001~900000123', 'd:rurn', '33000000123'
put 'sbr_control_db:local_unit_201801', '3210000001~900000123', 'd:ruref', '201801-ruref-900000123'
put 'sbr_control_db:local_unit_201801', '3210000001~900000123', 'd:name', '201801-name-900000123'
put 'sbr_control_db:local_unit_201801', '3210000001~900000123', 'd:trading_style', '201801-tradingstyle-900000123'
put 'sbr_control_db:local_unit_201801', '3210000001~900000123', 'd:address1', '201801-address1-900000123'
put 'sbr_control_db:local_unit_201801', '3210000001~900000123', 'd:address2', '201801-address2-900000123'
put 'sbr_control_db:local_unit_201801', '3210000001~900000123', 'd:address3', '201801-address3-900000123'
put 'sbr_control_db:local_unit_201801', '3210000001~900000123', 'd:address4', '201801-address4-900000123'
put 'sbr_control_db:local_unit_201801', '3210000001~900000123', 'd:address5', '201801-address5-900000123'
put 'sbr_control_db:local_unit_201801', '3210000001~900000123', 'd:postcode', '201801-postcode-900000123'
put 'sbr_control_db:local_unit_201801', '3210000001~900000123', 'd:sic07', '201801-sic07-900000123'
put 'sbr_control_db:local_unit_201801', '3210000001~900000123', 'd:employees', '123'
put 'sbr_control_db:local_unit_201801', '3210000001~900000123', 'd:employment', '55'
put 'sbr_control_db:local_unit_201801', '3210000001~900000123', 'd:prn', '0.946288337'
put 'sbr_control_db:local_unit_201801', '3210000001~900000123', 'd:region', '201801-region-2'

put 'sbr_control_db:local_unit_201802', '3210000001~900000123', 'd:lurn', '900000123'
put 'sbr_control_db:local_unit_201802', '3210000001~900000123', 'd:luref', '201802-luref-900000123'
put 'sbr_control_db:local_unit_201802', '3210000001~900000123', 'd:ern', '1000000123'
put 'sbr_control_db:local_unit_201802', '3210000001~900000123', 'd:entref', '201802-entref-900000123'
put 'sbr_control_db:local_unit_201802', '3210000001~900000123', 'd:rurn', '33000000123'
put 'sbr_control_db:local_unit_201802', '3210000001~900000123', 'd:ruref', '201802-ruref-900000123'
put 'sbr_control_db:local_unit_201802', '3210000001~900000123', 'd:name', '201802-name-900000123'
put 'sbr_control_db:local_unit_201802', '3210000001~900000123', 'd:trading_style', '201802-tradingstyle-900000123'
put 'sbr_control_db:local_unit_201802', '3210000001~900000123', 'd:address1', '201802-address1-900000123'
put 'sbr_control_db:local_unit_201802', '3210000001~900000123', 'd:address2', '201802-address2-900000123'
put 'sbr_control_db:local_unit_201802', '3210000001~900000123', 'd:address3', '201802-address3-900000123'
put 'sbr_control_db:local_unit_201802', '3210000001~900000123', 'd:address4', '201802-address4-900000123'
put 'sbr_control_db:local_unit_201802', '3210000001~900000123', 'd:address5', '201802-address5-900000123'
put 'sbr_control_db:local_unit_201802', '3210000001~900000123', 'd:postcode', '201802-postcode-900000123'
put 'sbr_control_db:local_unit_201802', '3210000001~900000123', 'd:sic07', '201802-sic07-900000123'
put 'sbr_control_db:local_unit_201802', '3210000001~900000123', 'd:employees', '123'
put 'sbr_control_db:local_unit_201802', '3210000001~900000123', 'd:employment', '55'
put 'sbr_control_db:local_unit_201802', '3210000001~900000123', 'd:prn', '0.946288337'
put 'sbr_control_db:local_unit_201802', '3210000001~900000123', 'd:region', '201802-region-2'
```

Insert some Reporting Unit data.

```shell
put 'sbr_control_db:reporting_unit_201801', '3210000001~33000000123', 'd:rurn', '33000000123'
put 'sbr_control_db:reporting_unit_201801', '3210000001~33000000123', 'd:ruref', '201801-ruref-33000000123'
put 'sbr_control_db:reporting_unit_201801', '3210000001~33000000123', 'd:ern', '1000000123'
put 'sbr_control_db:reporting_unit_201801', '3210000001~33000000123', 'd:entref', '201801-entref-33000000123'
put 'sbr_control_db:reporting_unit_201801', '3210000001~33000000123', 'd:name', '201801-name-33000000123'
put 'sbr_control_db:reporting_unit_201801', '3210000001~33000000123', 'd:trading_style', '201801-tradingstyle-33000000123'
put 'sbr_control_db:reporting_unit_201801', '3210000001~33000000123', 'd:legal_status', '201801-legalstatus-33000000123'
put 'sbr_control_db:reporting_unit_201801', '3210000001~33000000123', 'd:address1', '201801-address1-33000000123'
put 'sbr_control_db:reporting_unit_201801', '3210000001~33000000123', 'd:address2', '201801-address2-33000000123'
put 'sbr_control_db:reporting_unit_201801', '3210000001~33000000123', 'd:address3', '201801-address3-33000000123'
put 'sbr_control_db:reporting_unit_201801', '3210000001~33000000123', 'd:address4', '201801-address4-33000000123'
put 'sbr_control_db:reporting_unit_201801', '3210000001~33000000123', 'd:address5', '201801-address5-33000000123'
put 'sbr_control_db:reporting_unit_201801', '3210000001~33000000123', 'd:postcode', '201801-postcode-33000000123'
put 'sbr_control_db:reporting_unit_201801', '3210000001~33000000123', 'd:sic07', '201801-sic07-33000000123'
put 'sbr_control_db:reporting_unit_201801', '3210000001~33000000123', 'd:employees', '20'
put 'sbr_control_db:reporting_unit_201801', '3210000001~33000000123', 'd:employment', '22'
put 'sbr_control_db:reporting_unit_201801', '3210000001~33000000123', 'd:turnover', '200'
put 'sbr_control_db:reporting_unit_201801', '3210000001~33000000123', 'd:prn', '0.127347284'
put 'sbr_control_db:reporting_unit_201801', '3210000001~33000000123', 'd:region', '201801-region-2'

put 'sbr_control_db:reporting_unit_201802', '3210000001~33000000123', 'd:rurn', '33000000123'
put 'sbr_control_db:reporting_unit_201802', '3210000001~33000000123', 'd:ruref', '201802-ruref-33000000123'
put 'sbr_control_db:reporting_unit_201802', '3210000001~33000000123', 'd:ern', '1000000123'
put 'sbr_control_db:reporting_unit_201802', '3210000001~33000000123', 'd:entref', '201802-entref-33000000123'
put 'sbr_control_db:reporting_unit_201802', '3210000001~33000000123', 'd:name', '201802-name-33000000123'
put 'sbr_control_db:reporting_unit_201802', '3210000001~33000000123', 'd:trading_style', '201802-tradingstyle-33000000123'
put 'sbr_control_db:reporting_unit_201802', '3210000001~33000000123', 'd:legal_status', '201802-legalstatus-33000000123'
put 'sbr_control_db:reporting_unit_201802', '3210000001~33000000123', 'd:address1', '201802-address1-33000000123'
put 'sbr_control_db:reporting_unit_201802', '3210000001~33000000123', 'd:address2', '201802-address2-33000000123'
put 'sbr_control_db:reporting_unit_201802', '3210000001~33000000123', 'd:address3', '201802-address3-33000000123'
put 'sbr_control_db:reporting_unit_201802', '3210000001~33000000123', 'd:address4', '201802-address4-33000000123'
put 'sbr_control_db:reporting_unit_201802', '3210000001~33000000123', 'd:address5', '201802-address5-33000000123'
put 'sbr_control_db:reporting_unit_201802', '3210000001~33000000123', 'd:postcode', '201802-postcode-33000000123'
put 'sbr_control_db:reporting_unit_201802', '3210000001~33000000123', 'd:sic07', '201802-sic07-33000000123'
put 'sbr_control_db:reporting_unit_201802', '3210000001~33000000123', 'd:employees', '20'
put 'sbr_control_db:reporting_unit_201802', '3210000001~33000000123', 'd:employment', '22'
put 'sbr_control_db:reporting_unit_201802', '3210000001~33000000123', 'd:turnover', '200'
put 'sbr_control_db:reporting_unit_201802', '3210000001~33000000123', 'd:prn', '0.127347284'
put 'sbr_control_db:reporting_unit_201802', '3210000001~33000000123', 'd:region', '201802-region-2'
```

Insert some unit link data.

```shell
put 'sbr_control_db:unit_link_201801', 'ENT~1000000123', 'l:c_100001234500002', 'LEU'
put 'sbr_control_db:unit_link_201801', 'ENT~1000000123', 'l:c_900000123', 'LOU'
put 'sbr_control_db:unit_link_201801', 'ENT~1000000123', 'l:c_33000000123', 'REU'
put 'sbr_control_db:unit_link_201801', 'LEU~100001234500002', 'l:p_ENT', '1000000123'
put 'sbr_control_db:unit_link_201801', 'LOU~900000123', 'l:p_ENT', '1000000123'
put 'sbr_control_db:unit_link_201801', 'REU~33000000123', 'l:p_ENT', '1000000123'
put 'sbr_control_db:unit_link_201801', 'REU~33000000123', 'l:c_900000123', 'LOU'
put 'sbr_control_db:unit_link_201801', 'LOU~900000123', 'l:p_REU', '33000000123'
put 'sbr_control_db:unit_link_201801', 'LEU~100001234500002', 'l:c_23847563', 'CH'

put 'sbr_control_db:unit_link_201802', 'ENT~1000000123', 'l:c_100001234500002', 'LEU'
put 'sbr_control_db:unit_link_201802', 'ENT~1000000123', 'l:c_900000123', 'LOU'
put 'sbr_control_db:unit_link_201802', 'ENT~1000000123', 'l:c_33000000123', 'REU'
put 'sbr_control_db:unit_link_201802', 'LEU~100001234500002', 'l:p_ENT', '1000000123'
put 'sbr_control_db:unit_link_201802', 'LOU~900000123', 'l:p_ENT', '1000000123'
put 'sbr_control_db:unit_link_201802', 'REU~33000000123', 'l:p_ENT', '1000000123'
put 'sbr_control_db:unit_link_201802', 'REU~33000000123', 'l:c_900000123', 'LOU'
put 'sbr_control_db:unit_link_201802', 'LOU~900000123', 'l:p_REU', '33000000123'
put 'sbr_control_db:unit_link_201802', 'LEU~100001234500002', 'l:c_23847563', 'CH'
```

Run the API:

```shell
sbt run
```

Test the API routes:

[http://localhost:9000/v1/periods/201801/enterprises/1000000123](http://localhost:9000/v1/periods/201801/enterprises/1000000123)

[http://localhost:9000/v1/enterprises/1000000123/periods/201801/legalunits/1000012345000002](http://localhost:9000/v1/enterprises/1000000123/periods/201801/legalunits/1000012345000002)

[http://localhost:9000/v1/enterprises/1000000123/periods/201801/localunits/900000123](http://localhost:9000/v1/enterprises/1000000123/periods/201801/localunits/900000123)

[http://localhost:9000/v1/enterprises/1000000123/periods/201801/reportingunits/33000000123](http://localhost:9000/v1/enterprises/1000000123/periods/201801/reportingunits/33000000123)

[http://localhost:9000/v1/periods/201801/types/ENT/units/1000000123](http://localhost:9000/v1/periods/201801/types/ENT/units/1000000123)