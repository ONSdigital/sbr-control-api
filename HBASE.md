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

Create the two tables, within the namespace with a column family.

```shell
create 'sbr_control_db:enterprise', 'd'
create 'sbr_control_db:unit_links', 'l'
```

Insert some Enterprise data.

```shell
put 'sbr_control_db:enterprise' , '54321~201802', 'd:entref', '12345'
put 'sbr_control_db:enterprise' , '54321~201802', 'd:ent_name', 'Tesco'

put 'sbr_control_db:enterprise' , '54321~201801', 'd:entref', '12345'
put 'sbr_control_db:enterprise' , '54321~201801', 'd:ent_name', 'Tesco'

put 'sbr_control_db:enterprise' , '54321~201712', 'd:entref', '12345'
put 'sbr_control_db:enterprise' , '54321~201712', 'd:ent_name', 'Tesco'
```

Insert some unit link data.

```shell
put 'sbr_control_db:unit_links' , '12345~ENT~201802', 'l:c_192837465999', 'LEU'
put 'sbr_control_db:unit_links' , '12345~ENT~201802', 'l:c_23847563', 'CH'
put 'sbr_control_db:unit_links' , '12345~ENT~201802', 'l:c_38576395', 'PAYE'
put 'sbr_control_db:unit_links' , '12345~ENT~201802', 'l:c_41037492', 'VAT'

put 'sbr_control_db:unit_links' , '12345~ENT~201801', 'l:c_192837465999', 'LEU'
put 'sbr_control_db:unit_links' , '12345~ENT~201801', 'l:c_23847563', 'CH'
put 'sbr_control_db:unit_links' , '12345~ENT~201801', 'l:c_38576395', 'PAYE'
put 'sbr_control_db:unit_links' , '12345~ENT~201801', 'l:c_41037492', 'VAT'

put 'sbr_control_db:unit_links' , '192837465999~LEU~201802', 'l:p_ENT', '12345'
put 'sbr_control_db:unit_links' , '192837465999~LEU~201802', 'l:c_23847563', 'CH'
put 'sbr_control_db:unit_links' , '192837465999~LEU~201802', 'l:c_38576395', 'PAYE'
put 'sbr_control_db:unit_links' , '192837465999~LEU~201802', 'l:c_41037492', 'VAT'

put 'sbr_control_db:unit_links' , '192837465999~LEU~201801', 'l:p_ENT', '12345'
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

Use the following URL's to test HBase REST:

[http://localhost:8080/sbr_control_db:enterprise/54321~*/d](http://localhost:8080/sbr_control_db:enterprise/54321~*/d)
[http://localhost:8080/sbr_control_db:enterprise/54321~201802/d](http://localhost:8080/sbr_control_db:enterprise/54321~201802/d)
[http://localhost:8080/sbr_control_db:unit_links/12345~*/l](http://localhost:8080/sbr_control_db:unit_links/12345~*/l)
[http://localhost:8080/sbr_control_db:unit_links/12345~ENT~201802/l](http://localhost:8080/sbr_control_db:unit_links/12345~ENT~201802/l)

Run the API:

```shell
sbt run
```

Test the API routes:

[http://localhost:9000/v1/units/12345](http://localhost:9000/v1/units/12345)
[http://localhost:9000/v1/periods/201802/types/ENT/units/12345](http://localhost:9000/v1/periods/201802/types/ENT/units/12345)
[http://localhost:9000/v1/enterprises/12345](http://localhost:9000/v1/enterprises/12345)
[http://localhost:9000/v1/enterprises/12345](http://localhost:9000/v1/enterprises/12345)