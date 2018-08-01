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

```shell
create 'sbr_control_db:enterprise', 'd'
create 'sbr_control_db:unit_links', 'l'
create 'sbr_control_db:local_unit', 'd'
create 'sbr_control_db:legal_unit_201801', 'd'
```

Insert some Enterprise data.

```shell
put 'sbr_control_db:enterprise' , '3210000001~201802', 'd:ern', '1000000123'
put 'sbr_control_db:enterprise' , '3210000001~201802', 'd:entref', 'idbr-1000000123'
put 'sbr_control_db:enterprise' , '3210000001~201802', 'd:name', 'Tesco'
put 'sbr_control_db:enterprise' , '3210000001~201802', 'd:postcode', 'AB10 5BD'
put 'sbr_control_db:enterprise' , '3210000001~201802', 'd:legalstatus', 'A'
put 'sbr_control_db:enterprise' , '3210000001~201802', 'd:paye_employees', '1'
put 'sbr_control_db:enterprise' , '3210000001~201802', 'd:paye_jobs', '5'

put 'sbr_control_db:enterprise' , '3210000001~201801', 'd:ern', '1000000123'
put 'sbr_control_db:enterprise' , '3210000001~201801', 'd:entref', 'idbr-1000000123'
put 'sbr_control_db:enterprise' , '3210000001~201801', 'd:name', 'Tesco'
put 'sbr_control_db:enterprise' , '3210000001~201801', 'd:postcode', 'AB10 5BD'
put 'sbr_control_db:enterprise' , '3210000001~201801', 'd:legalstatus', 'A'
put 'sbr_control_db:enterprise' , '3210000001~201801', 'd:paye_employees', '1'
put 'sbr_control_db:enterprise' , '3210000001~201801', 'd:paye_jobs', '5'
```

Insert some Local Unit data.

```shell
put 'sbr_control_db:local_unit', '3210000001~201802~900000123', 'd:lurn', '900000123'
put 'sbr_control_db:local_unit', '3210000001~201802~900000123', 'd:luref', 'luref-900000123'
put 'sbr_control_db:local_unit', '3210000001~201802~900000123', 'd:ern', '1000000123'
put 'sbr_control_db:local_unit', '3210000001~201802~900000123', 'd:entref', 'entref-123'
put 'sbr_control_db:local_unit', '3210000001~201802~900000123', 'd:name', 'Company 123'
put 'sbr_control_db:local_unit', '3210000001~201802~900000123', 'd:trading_style', 'A'
put 'sbr_control_db:local_unit', '3210000001~201802~900000123', 'd:address1', 'address1-123'
put 'sbr_control_db:local_unit', '3210000001~201802~900000123', 'd:address2', 'address2-123'
put 'sbr_control_db:local_unit', '3210000001~201802~900000123', 'd:address3', 'address3-123'
put 'sbr_control_db:local_unit', '3210000001~201802~900000123', 'd:address4', 'address4-123'
put 'sbr_control_db:local_unit', '3210000001~201802~900000123', 'd:address5', 'address5-123'
put 'sbr_control_db:local_unit', '3210000001~201802~900000123', 'd:postcode', 'postcode-123'
put 'sbr_control_db:local_unit', '3210000001~201802~900000123', 'd:sic07', 'sic07-123'
put 'sbr_control_db:local_unit', '3210000001~201802~900000123', 'd:employees', '123'


put 'sbr_control_db:local_unit', '3210000001~201801~900000123', 'd:lurn', '900000123'
put 'sbr_control_db:local_unit', '3210000001~201801~900000123', 'd:luref', 'luref-900000123'
put 'sbr_control_db:local_unit', '3210000001~201801~900000123', 'd:ern', '1000000123'
put 'sbr_control_db:local_unit', '3210000001~201801~900000123', 'd:entref', 'entref-123'
put 'sbr_control_db:local_unit', '3210000001~201801~900000123', 'd:name', 'Company 123'
put 'sbr_control_db:local_unit', '3210000001~201801~900000123', 'd:trading_style', 'A'
put 'sbr_control_db:local_unit', '3210000001~201801~900000123', 'd:address1', 'address1-123'
put 'sbr_control_db:local_unit', '3210000001~201801~900000123', 'd:address2', 'address2-123'
put 'sbr_control_db:local_unit', '3210000001~201801~900000123', 'd:address3', 'address3-123'
put 'sbr_control_db:local_unit', '3210000001~201801~900000123', 'd:address4', 'address4-123'
put 'sbr_control_db:local_unit', '3210000001~201801~900000123', 'd:address5', 'address5-123'
put 'sbr_control_db:local_unit', '3210000001~201801~900000123', 'd:postcode', 'postcode-123'
put 'sbr_control_db:local_unit', '3210000001~201801~900000123', 'd:sic07', 'sic07-123'
put 'sbr_control_db:local_unit', '3210000001~201801~900000123', 'd:employees', '123'
```

Insert some unit link data.

```shell
put 'sbr_control_db:unit_links' , '1000000123~ENT~201802', 'l:c_192837465999', 'LEU'
put 'sbr_control_db:unit_links' , '1000000123~ENT~201802', 'l:c_900000123', 'LOU'
put 'sbr_control_db:unit_links' , '1000000123~ENT~201802', 'l:c_23847563', 'CH'
put 'sbr_control_db:unit_links' , '1000000123~ENT~201802', 'l:c_38576395', 'PAYE'
put 'sbr_control_db:unit_links' , '1000000123~ENT~201802', 'l:c_41037492', 'VAT'

put 'sbr_control_db:unit_links' , '1000000123~ENT~201801', 'l:c_192837465999', 'LEU'
put 'sbr_control_db:unit_links' , '1000000123~ENT~201801', 'l:c_900000123', 'LOU'
put 'sbr_control_db:unit_links' , '1000000123~ENT~201801', 'l:c_23847563', 'CH'
put 'sbr_control_db:unit_links' , '1000000123~ENT~201801', 'l:c_38576395', 'PAYE'
put 'sbr_control_db:unit_links' , '1000000123~ENT~201801', 'l:c_41037492', 'VAT'

put 'sbr_control_db:unit_links' , '900000123~LOU~201802', 'l:p_ENT', '1000000123'
put 'sbr_control_db:unit_links' , '900000123~LOU~201801', 'l:p_ENT', '1000000123'

put 'sbr_control_db:unit_links' , '192837465999~LEU~201802', 'l:p_ENT', '1000000123'
put 'sbr_control_db:unit_links' , '192837465999~LEU~201802', 'l:c_23847563', 'CH'
put 'sbr_control_db:unit_links' , '192837465999~LEU~201802', 'l:c_38576395', 'PAYE'
put 'sbr_control_db:unit_links' , '192837465999~LEU~201802', 'l:c_41037492', 'VAT'

put 'sbr_control_db:unit_links' , '192837465999~LEU~201801', 'l:p_ENT', '1000000123'
put 'sbr_control_db:unit_links' , '192837465999~LEU~201801', 'l:c_23847563', 'CH'
put 'sbr_control_db:unit_links' , '192837465999~LEU~201801', 'l:c_38576395', 'PAYE'
put 'sbr_control_db:unit_links' , '192837465999~LEU~201801', 'l:c_41037492', 'VAT'

put 'sbr_control_db:unit_links' , '23847563~CH~201802', 'l:p_LEU', '192837465999'
put 'sbr_control_db:unit_links' , '23847563~CH~201801', 'l:p_LEU', '192837465999'

put 'sbr_control_db:unit_links' , '41037492~VAT~201802', 'l:p_LEU', '192837465999'
put 'sbr_control_db:unit_links' , '41037492~VAT~201801', 'l:p_LEU', '192837465999'

put 'sbr_control_db:unit_links' , '38576395~PAYE~201802', 'l:p_LEU', '192837465999'
put 'sbr_control_db:unit_links' , '38576395~PAYE~201801', 'l:p_LEU', '192837465999'
```

Insert some Legal Unit data.

```shell
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:ubrn', '1000012345000002'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:name', 'Company-2'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:trading_style', 'tradingStyle-2'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:address1', 'address1-2'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:address2', 'address2-2'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:address3', 'address3-2'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:address4', 'address4-2'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:address5', 'address5-2'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:postcode', 'postcode-2'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:sic07', 'sic07-2'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:paye_jobs', '2'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:turnover', '20'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:legal_status', 'legalStatus-2'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:trading_status', 'tradingStatus-2'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:birth_date', 'birthDate-2'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:death_date', 'deathDate-2'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:death_code', 'deathCode-2'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:crn', 'crn-2'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000002', 'd:uprn', 'uprn-2'

put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000001', 'd:ubrn', '1000012345000001'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000001', 'd:name', 'Company-1'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000001', 'd:address1', 'address1-1'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000001', 'd:postcode', 'postcode-1'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000001', 'd:sic07', 'sic07-1'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000001', 'd:legal_status', 'legalStatus-1'
put 'sbr_control_db:legal_unit_201801', '3210000001~1000012345000001', 'd:birth_date', 'birthDate-1'
```

Use the following URL's to test HBase REST:

[http://localhost:8080/sbr_control_db:enterprise/3210000001~*/d](http://localhost:8080/sbr_control_db:enterprise/3210000001~*/d)<br/>
[http://localhost:8080/sbr_control_db:enterprise/3210000001~201802/d](http://localhost:8080/sbr_control_db:enterprise/3210000001~201802/d)<br/>
[http://localhost:8080/sbr_control_db:unit_links/1000000123~*/l](http://localhost:8080/sbr_control_db:unit_links/1000000123~*/l)<br/>
[http://localhost:8080/sbr_control_db:unit_links/1000000123~ENT~201802/l](http://localhost:8080/sbr_control_db:unit_links/1000000123~ENT~201802/l)

Run the API:

```shell
sbt run
```

Test the API routes:

[http://localhost:9000/v1/units/1000000123](http://localhost:9000/v1/units/1000000123)<br/>
[http://localhost:9000/v1/periods/201802/types/ENT/units/1000000123](http://localhost:9000/v1/periods/201802/types/ENT/units/1000000123)<br/>
[http://localhost:9000/v1/enterprises/1000000123](http://localhost:9000/v1/enterprises/1000000123)<br/>
[http://localhost:9000/v1/periods/201802/enterprises/1000000123](http://localhost:9000/v1/periods/201802/enterprises/1000000123)