# bows_formula_one

This project is a Scala wb application built for handling Json API calls to MongoDB 
database using ReactiveMongo.

You will need  and instance of MongoDb running eg Compass

To run the tests manually use Postman wth URL localhost9000: followed by the endpoints below:

Action                   Http verb         Information                                                  Endpoint

Start/End session   |GET            uses the card Id to check MongoDb for a user session              | present/id
                                          and ther starts a new session or ends an existong session
                                          
                                          
Add a new member    |POST           sends Json data to MongoDb, checks for an existing                | addNewMember
                                          member with  _id in Jso data and either creates a 
                                          new member or returns relevant error message
                                          
Get member detils   |GET            sends _id  to check MongoDB for a member then if member           | getMember/_id
                                          exists return all dta for that member , if no member exists
                                          then returns relevant error message
                                          
Get balance         |GET            uses _id to check is member exists on MongoDB and if              |getBalance/_id
                                          member exists returns balance if not returns error message
                                          
Delete member       |POST           uses _id to check is member exists on MongoDB and if              |deleteMember/_id
                                          member exists delete records if no member exists returns
                                          relevant error message
                                          
Update name         |POST          uses _id to check is member exists on MongoDB and if               |updateName/_id/newName
                                         member exists updates name field with given name if no
                                         member exists return error message
                                         
Increase balance    |POST          uses _id to check is member exists on MongoDB and if               |increaseBalance/_id/amount
                                   member exists updates and increases balance field by
                                   amount given if no members exists gives error message
                                   
Decrease balance    |POST          uses _id to check is member exists on MongoDB and if               |increaseBalance/_id/amount
                                   member exists updates and decreases balance field by
                                   amount given if no members exists gives error message. 
                                   If decrese is larger than existing balance gives error 
                                   message
                                   

                                          
                                          

            
