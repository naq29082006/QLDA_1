const mongoose = require('mongoose');
const { Users, Categories, Products, Orders, OrderDetails } = require('./models/database');

// Kết nối MongoDB
const local = 'mongodb://127.0.0.1:27017/Duan1';

const connect = async () => {
    try {
        await mongoose.connect(local, {
            useNewUrlParser: true,
            useUnifiedTopology: true,
        });
        console.log('✅ Kết nối MongoDB thành công');
    } catch (error) {
        console.error('❌ Lỗi kết nối MongoDB:', error);
        process.exit(1);
    }
};

// Xóa dữ liệu cũ (tùy chọn)
const clearData = async () => {
    try {
        await OrderDetails.deleteMany({});
        await Orders.deleteMany({});
        await Products.deleteMany({});
        await Categories.deleteMany({});
        await Users.deleteMany({});
        console.log('✅ Đã xóa dữ liệu cũ');
    } catch (error) {
        console.error('❌ Lỗi xóa dữ liệu:', error);
    }
};

// Thêm dữ liệu mẫu
const seedData = async () => {
    try {
        console.log('📦 Đang thêm dữ liệu mẫu...\n');

        // 1. Thêm Users
        console.log('👤 Đang thêm Users...');
        const user1 = await Users.create({
            name: 'Nguyễn Văn A',
            email: 'admin@example.com',
            password: '123456',
            phone: '0123456789'
        });
        const user2 = await Users.create({
            name: 'Trần Thị B',
            email: 'user@example.com',
            password: '123456',
            phone: '0987654321'
        });
        const user3 = await Users.create({
            name: 'Lê Văn C',
            email: 'customer@example.com',
            password: '123456',
            phone: '0912345678'
        });
        console.log(`✅ Đã thêm ${await Users.countDocuments()} users\n`);

        // 2. Thêm Categories
        console.log('📂 Đang thêm Categories...');
        const category1 = await Categories.create({
            name: 'Nước ép',
            description: 'Các loại nước ép tươi'
        });
        const category2 = await Categories.create({
            name: 'Sinh tố',
            description: 'Các loại sinh tố thơm ngon'
        });
        const category3 = await Categories.create({
            name: 'Nước giải khát',
            description: 'Các loại nước giải khát'
        });
        console.log(`✅ Đã thêm ${await Categories.countDocuments()} categories\n`);

        // 3. Thêm Products
        console.log('🛍️ Đang thêm Products...');
        const products = [
            {
                name: 'nước ép',
                description: 'Nước ép tươi nguyên chất',
                price: 250000,
                image: '',
                category_id: category1._id
            },
            {
                name: 'nước sinh tố',
                description: 'Sinh tố thơm ngon',
                price: 150000,
                image: '',
                category_id: category2._id
            },
            {
                name: 'nước ép cam',
                description: 'Nước ép cam tươi',
                price: 99000,
                image: '',
                category_id: category1._id
            },
            {
                name: 'nước ép táo',
                description: 'Nước ép táo nguyên chất',
                price: 120000,
                image: '',
                category_id: category1._id
            },
            {
                name: 'nước ép dưa hấu',
                description: 'Nước ép dưa hấu mát lạnh',
                price: 80000,
                image: '',
                category_id: category1._id
            },
            {
                name: 'sinh tố xoài',
                description: 'Sinh tố xoài thơm ngon',
                price: 180000,
                image: '',
                category_id: category2._id
            },
            {
                name: 'sinh tố dâu',
                description: 'Sinh tố dâu tây',
                price: 200000,
                image: '',
                category_id: category2._id
            },
            {
                name: 'sinh tố chuối',
                description: 'Sinh tố chuối bổ dưỡng',
                price: 160000,
                image: '',
                category_id: category2._id
            },
            {
                name: 'nước ép cà rốt',
                description: 'Nước ép cà rốt tốt cho mắt',
                price: 110000,
                image: '',
                category_id: category1._id
            },
            {
                name: 'nước ép dứa',
                description: 'Nước ép dứa thơm mát',
                price: 95000,
                image: '',
                category_id: category1._id
            }
        ];

        const createdProducts = await Products.insertMany(products);
        console.log(`✅ Đã thêm ${createdProducts.length} products\n`);

        // 4. Thêm Orders
        console.log('📦 Đang thêm Orders...');
        const orders = [
            {
                user_id: user1._id,
                total_price: 500000,
                status: 'Đã giao',
                receiver_name: 'Nguyễn Văn A',
                receiver_address: '123 Đường ABC, Quận 1, TP.HCM',
                receiver_phone: '0123456789'
            },
            {
                user_id: user2._id,
                total_price: 750000,
                status: 'Đang chờ',
                receiver_name: 'Trần Thị B',
                receiver_address: '456 Đường XYZ, Quận 2, TP.HCM',
                receiver_phone: '0987654321'
            },
            {
                user_id: user1._id,
                total_price: 300000,
                status: 'Đang chuẩn bị',
                receiver_name: 'Nguyễn Văn A',
                receiver_address: '123 Đường ABC, Quận 1, TP.HCM',
                receiver_phone: '0123456789'
            },
            {
                user_id: user3._id,
                total_price: 1200000,
                status: 'Đang giao',
                receiver_name: 'Lê Văn C',
                receiver_address: '789 Đường DEF, Quận 3, TP.HCM',
                receiver_phone: '0912345678'
            },
            {
                user_id: user2._id,
                total_price: 900000,
                status: 'Đang chờ',
                receiver_name: 'Trần Thị B',
                receiver_address: '456 Đường XYZ, Quận 2, TP.HCM',
                receiver_phone: '0987654321'
            },
            {
                user_id: user1._id,
                total_price: 450000,
                status: 'Đã giao',
                receiver_name: 'Nguyễn Văn A',
                receiver_address: '123 Đường ABC, Quận 1, TP.HCM',
                receiver_phone: '0123456789'
            },
            {
                user_id: user3._id,
                total_price: 680000,
                status: 'Đang chuẩn bị',
                receiver_name: 'Lê Văn C',
                receiver_address: '789 Đường DEF, Quận 3, TP.HCM',
                receiver_phone: '0912345678'
            },
            {
                user_id: user2._id,
                total_price: 550000,
                status: 'Hủy',
                receiver_name: 'Trần Thị B',
                receiver_address: '456 Đường XYZ, Quận 2, TP.HCM',
                receiver_phone: '0987654321'
            }
        ];

        const createdOrders = await Orders.insertMany(orders);
        console.log(`✅ Đã thêm ${createdOrders.length} orders\n`);

        // 5. Thêm OrderDetails
        console.log('📋 Đang thêm OrderDetails...');
        const orderDetails = [
            // Order 1
            {
                order_id: createdOrders[0]._id,
                product_id: createdProducts[0]._id,
                quantity: 2,
                price: createdProducts[0].price,
                subtotal: createdProducts[0].price * 2
            },
            // Order 2
            {
                order_id: createdOrders[1]._id,
                product_id: createdProducts[1]._id,
                quantity: 5,
                price: createdProducts[1].price,
                subtotal: createdProducts[1].price * 5
            },
            // Order 3
            {
                order_id: createdOrders[2]._id,
                product_id: createdProducts[2]._id,
                quantity: 3,
                price: createdProducts[2].price,
                subtotal: createdProducts[2].price * 3
            },
            // Order 4
            {
                order_id: createdOrders[3]._id,
                product_id: createdProducts[0]._id,
                quantity: 2,
                price: createdProducts[0].price,
                subtotal: createdProducts[0].price * 2
            },
            {
                order_id: createdOrders[3]._id,
                product_id: createdProducts[1]._id,
                quantity: 3,
                price: createdProducts[1].price,
                subtotal: createdProducts[1].price * 3
            },
            {
                order_id: createdOrders[3]._id,
                product_id: createdProducts[5]._id,
                quantity: 2,
                price: createdProducts[5].price,
                subtotal: createdProducts[5].price * 2
            },
            // Order 5
            {
                order_id: createdOrders[4]._id,
                product_id: createdProducts[3]._id,
                quantity: 4,
                price: createdProducts[3].price,
                subtotal: createdProducts[3].price * 4
            },
            {
                order_id: createdOrders[4]._id,
                product_id: createdProducts[4]._id,
                quantity: 3,
                price: createdProducts[4].price,
                subtotal: createdProducts[4].price * 3
            },
            // Order 6
            {
                order_id: createdOrders[5]._id,
                product_id: createdProducts[2]._id,
                quantity: 2,
                price: createdProducts[2].price,
                subtotal: createdProducts[2].price * 2
            },
            {
                order_id: createdOrders[5]._id,
                product_id: createdProducts[4]._id,
                quantity: 2,
                price: createdProducts[4].price,
                subtotal: createdProducts[4].price * 2
            },
            // Order 7
            {
                order_id: createdOrders[6]._id,
                product_id: createdProducts[6]._id,
                quantity: 2,
                price: createdProducts[6].price,
                subtotal: createdProducts[6].price * 2
            },
            {
                order_id: createdOrders[6]._id,
                product_id: createdProducts[7]._id,
                quantity: 2,
                price: createdProducts[7].price,
                subtotal: createdProducts[7].price * 2
            },
            // Order 8
            {
                order_id: createdOrders[7]._id,
                product_id: createdProducts[8]._id,
                quantity: 3,
                price: createdProducts[8].price,
                subtotal: createdProducts[8].price * 3
            },
            {
                order_id: createdOrders[7]._id,
                product_id: createdProducts[9]._id,
                quantity: 2,
                price: createdProducts[9].price,
                subtotal: createdProducts[9].price * 2
            }
        ];

        await OrderDetails.insertMany(orderDetails);
        console.log(`✅ Đã thêm ${orderDetails.length} order details\n`);

        console.log('🎉 Hoàn thành! Dữ liệu đã được thêm vào MongoDB\n');
        console.log('📊 Tổng kết:');
        console.log(`   - Users: ${await Users.countDocuments()}`);
        console.log(`   - Categories: ${await Categories.countDocuments()}`);
        console.log(`   - Products: ${await Products.countDocuments()}`);
        console.log(`   - Orders: ${await Orders.countDocuments()}`);
        console.log(`   - OrderDetails: ${await OrderDetails.countDocuments()}\n`);

    } catch (error) {
        console.error('❌ Lỗi thêm dữ liệu:', error);
    }
};

// Chạy script
const run = async () => {
    await connect();
    
    // Hỏi có muốn xóa dữ liệu cũ không
    const readline = require('readline').createInterface({
        input: process.stdin,
        output: process.stdout
    });

    readline.question('Bạn có muốn xóa dữ liệu cũ trước khi thêm mới? (y/n): ', async (answer) => {
        if (answer.toLowerCase() === 'y' || answer.toLowerCase() === 'yes') {
            await clearData();
        }
        await seedData();
        readline.close();
        mongoose.connection.close();
        console.log('✅ Đã đóng kết nối MongoDB');
        process.exit(0);
    });
};

run();

